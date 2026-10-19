package org.example.inventory.Presentation.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.inventory.Domain.Model.VietfulProduct.Barcode;
import org.example.inventory.Domain.Model.VietfulProduct.Bundle;
import org.example.inventory.Domain.Model.VietfulProduct.Category;
import org.example.inventory.Domain.Model.VietfulProduct.Unit;

import java.util.List;

public record VietfulProductDetailResponse(
        int productId,
        String productName,
        String sku,
        String outboundType,
        @JsonProperty("isExpiryDate") boolean isExpiryDate,
        @JsonProperty("isAttribute") boolean isAttribute,
        @JsonProperty("isLotNumber") boolean isLotNumber,
        @JsonProperty("isTrackingSerial") boolean isTrackingSerial,
        int numDaysWarningExpDate,
        int numDaysExportBeforeExpDate,
        String packingNote,
        String avatarURL,
        String assetType,
        @JsonProperty("isPhysical") boolean isPhysical,
        String partnerSKU,
        boolean hasSerial,
        String color,
        String size,
        String description,
        @JsonProperty("isActive") boolean isActive,
        List<Unit> units,
        List<Category> categories,
        List<Bundle> productBundles,
        List<Barcode> productBarcodes,
        @JsonProperty("isDiscreet") boolean isDiscreet,
        String parentPartnerSKU,
        String hsCode,
        String countryOfOrigin,
        String refCode
) { }
