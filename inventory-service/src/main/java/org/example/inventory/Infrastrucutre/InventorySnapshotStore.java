package org.example.inventory.Infrastrucutre;

import org.example.inventory.Domain.Model.VietfulProduct;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;

public class InventorySnapshotStore {
    private final Path stateFile;
    private final JsonMapper json = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();

    public InventorySnapshotStore(Path stateFile) {
        this.stateFile = stateFile.toAbsolutePath().normalize();
    }

    public List<VietfulProduct> reconcile(List<VietfulProduct> generated) {
        // JVM lock plus file lock: serialize initialization in this JVM and across processes.
        synchronized (InventorySnapshotStore.class) {
            try {
                Files.createDirectories(stateFile.getParent());
                Path lockFile = stateFile.resolveSibling(stateFile.getFileName() + ".lock");
                try (var channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                     var lock = channel.lock()) {
                    var previous = new HashMap<Integer, VietfulProduct>();
                    if (Files.exists(stateFile)) {
                        for (var product : json.readValue(Files.readAllBytes(stateFile), VietfulProduct[].class)) {
                            if (previous.put(product.productId(), product) != null) {
                                throw new IllegalStateException("Duplicate productId in inventory state file");
                            }
                        }
                    }
                    // Bootstrap once above the small counters used by the earlier CDMS demo.
                    // Subsequent versions come from the persisted counter, not the clock.
                    long initialRevision = Math.max(1, System.currentTimeMillis());
                    var products = generated.stream().map(candidate -> {
                        var stored = previous.get(candidate.productId());
                        if (stored != null && stored.withRevision(1).equals(candidate.withRevision(1))) return stored;
                        long revision = stored == null ? initialRevision : Math.incrementExact(stored.revision());
                        return candidate.withRevision(revision);
                    }).toList();
                    persist(products);
                    return products;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot read/write inventory state at " + stateFile
                        + "; refusing to reset source revisions", exception);
            }
        }
    }

    private void persist(List<VietfulProduct> products) throws IOException {
        Path temporary = Files.createTempFile(stateFile.getParent(), "inventory-", ".tmp");
        try {
            try (var channel = FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                var bytes = ByteBuffer.wrap(json.writeValueAsBytes(products));
                while (bytes.hasRemaining()) channel.write(bytes);
                channel.force(true);
            }
            Files.move(temporary, stateFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
