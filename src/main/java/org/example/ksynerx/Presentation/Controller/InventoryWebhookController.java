package org.example.ksynerx.Presentation.Controller;

import jakarta.validation.Valid;
import org.example.ksynerx.Application.Dto.Command.EnqueueInventoryProductCommand;
import org.example.ksynerx.Application.Usecase.EnqueueInventoryProductUseCase;
import org.example.ksynerx.Domain.Model.ChannelType;
import org.example.ksynerx.Presentation.Contract.ApiResponse;
import org.example.ksynerx.Presentation.Dto.Request.InventoryProductWebhookRequest;
import org.example.ksynerx.Presentation.Dto.Response.InventoryWebhookAcceptedResponse;
import org.example.ksynerx.Presentation.Mapper.InventoryProductWebhookMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/inventory/products")
@ConditionalOnProperty(prefix = "integrations.inventory.webhook", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InventoryWebhookController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryWebhookController.class);
    private final EnqueueInventoryProductUseCase inbox;
    private final String sourceCode;

    public InventoryWebhookController(EnqueueInventoryProductUseCase inbox,
                                       @Value("${integrations.inventory.source-code:VIETFUL_DEMO}") String sourceCode) {
        this.inbox = inbox;
        this.sourceCode = sourceCode;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<InventoryWebhookAcceptedResponse>> receive(
            @Valid @RequestBody InventoryProductWebhookRequest request) {
        var product = InventoryProductWebhookMapper.toProduct(request);
        var event = inbox.execute(new EnqueueInventoryProductCommand(sourceCode, ChannelType.WEBHOOK, product));
        // The transactional use case has committed before acknowledging receipt.
        LOGGER.info("Webhook product enqueued source_code={} product_id={} event_id={} revision={} content_hash={}",
                sourceCode, product.productId(), event.eventId(), event.revision(), event.contentHash());
        return ResponseEntity.accepted().body(ApiResponse.success(new InventoryWebhookAcceptedResponse(
                event.eventId(), event.channelId(), product.productId(), event.revision(), event.contentHash())));
    }
}
