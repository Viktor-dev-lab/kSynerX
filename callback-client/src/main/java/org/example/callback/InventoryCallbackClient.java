package org.example.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class InventoryCallbackClient implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryCallbackClient.class);
    private final CallbackClientProperties properties;
    private final JsonMapper json = JsonMapper.builder().build();

    public InventoryCallbackClient(CallbackClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        try (var http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(properties.timeoutMs())).build()) {
            var snapshots = load(http);
            if (hasText(properties.saveSnapshotFile())) {
                Path file = Path.of(properties.saveSnapshotFile()).toAbsolutePath();
                Files.createDirectories(file.getParent());
                Files.writeString(file, "[" + String.join(",", snapshots.stream().map(Snapshot::payload).toList()) + "]",
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                LOGGER.info("Saved {} snapshots to {}", snapshots.size(), file);
            }
            int accepted = 0;
            int failed = 0;
            try (var threads = Executors.newFixedThreadPool(properties.concurrency())) {
                List<Future<Boolean>> deliveries = new ArrayList<>();
                for (int repeat = 0; repeat < properties.repeat(); repeat++) {
                    for (var snapshot : snapshots) deliveries.add(threads.submit(() -> deliver(http, snapshot)));
                }
                for (var delivery : deliveries) {
                    if (delivery.get()) accepted++;
                    else failed++;
                }
            }
            LOGGER.info("Callback completed accepted={} failed={}", accepted, failed);
            if (failed > 0) throw new IllegalStateException("Some webhook deliveries were not accepted; replay the saved snapshots to retry");
        }
    }

    private List<Snapshot> load(HttpClient http) throws IOException, InterruptedException {
        String body;
        if (hasText(properties.snapshotFile())) {
            body = Files.readString(Path.of(properties.snapshotFile()), StandardCharsets.UTF_8);
        } else {
            String url = properties.inventoryBaseUrl().replaceAll("/+$", "") + "/api/v1/Products"
                    + (properties.productId() == null ? "" : "/" + properties.productId());
            var response = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMillis(properties.timeoutMs()))
                    .GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) throw new IOException("Inventory returned HTTP " + response.statusCode());
            body = response.body();
        }
        JsonNode root = json.readTree(body);
        List<Snapshot> snapshots = new ArrayList<>();
        if (root != null && root.isArray()) {
            for (var product : root) add(snapshots, product);
        } else {
            add(snapshots, root);
        }
        if (snapshots.isEmpty()) throw new IllegalArgumentException("No matching product snapshots found");
        return List.copyOf(snapshots);
    }

    private void add(List<Snapshot> snapshots, JsonNode product) {
        if (product == null || !product.isObject() || !product.has("productId") || !product.has("revision")
                || !product.get("productId").isIntegralNumber() || !product.get("revision").isIntegralNumber()
                || product.get("revision").longValue() <= 0) {
            throw new IllegalArgumentException("Each snapshot must contain an integer productId and positive source revision");
        }
        long productId = product.get("productId").longValue();
        if (properties.productId() != null && productId != properties.productId()) return;
        snapshots.add(new Snapshot(productId, product.get("revision").longValue(), json.writeValueAsString(product)));
    }

    private boolean deliver(HttpClient http, Snapshot snapshot) throws InterruptedException {
        // Freeze the entire snapshot before retrying; never fetch a newer version for a retry.
        var request = HttpRequest.newBuilder(URI.create(properties.webhookUrl()))
                .timeout(Duration.ofMillis(properties.timeoutMs())).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(snapshot.payload(), StandardCharsets.UTF_8)).build();
        for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
            try {
                var response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() == 202) {
                    var acknowledgement = json.readTree(response.body());
                    var data = acknowledgement == null ? null : acknowledgement.get("data");
                    if (data == null || !acknowledgement.path("success").asBoolean(false)
                            || data.path("eventId").longValue() <= 0
                            || data.path("productId").longValue() != snapshot.productId()
                            || data.path("revision").longValue() != snapshot.revision()) {
                        throw new IOException("Invalid webhook acknowledgement");
                    }
                    LOGGER.info("Webhook accepted product_id={} revision={} event_id={} attempt={}",
                            snapshot.productId(), snapshot.revision(), data.path("eventId").longValue(), attempt);
                    return true;
                }
                if (response.statusCode() != 408 && response.statusCode() != 429 && response.statusCode() < 500) {
                    LOGGER.error("Webhook rejected product_id={} revision={} http_status={}",
                            snapshot.productId(), snapshot.revision(), response.statusCode());
                    return false;
                }
                LOGGER.warn("Webhook temporary failure product_id={} revision={} http_status={} attempt={}",
                        snapshot.productId(), snapshot.revision(), response.statusCode(), attempt);
            } catch (IOException | JacksonException exception) {
                LOGGER.warn("Webhook delivery error product_id={} revision={} attempt={} reason={}",
                        snapshot.productId(), snapshot.revision(), attempt, exception.getMessage());
            }
            if (attempt < properties.maxAttempts()) Thread.sleep(properties.retryDelayMs());
        }
        LOGGER.error("Webhook retries exhausted product_id={} revision={}", snapshot.productId(), snapshot.revision());
        return false;
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }
    private record Snapshot(long productId, long revision, String payload) { }
}
