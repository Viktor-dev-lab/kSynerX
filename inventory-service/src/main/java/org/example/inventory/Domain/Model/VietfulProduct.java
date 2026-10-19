package org.example.inventory.Domain.Model;

import java.math.BigDecimal;
import java.util.List;

public record VietfulProduct(
        int productId,
        String productName,
        String sku,
        String outboundType,
        boolean isExpiryDate,
        boolean isAttribute,
        boolean isLotNumber,
        boolean isTrackingSerial,
        int numDaysWarningExpDate,
        int numDaysExportBeforeExpDate,
        String packingNote,
        String avatarURL,
        String assetType,
        boolean isPhysical,
        String partnerSKU,
        boolean hasSerial,
        String color,
        String size,
        String description,
        boolean isActive,
        List<Unit> units,
        List<Category> categories,
        List<Bundle> productBundles,
        List<Barcode> productBarcodes,
        boolean isDiscreet,
        String parentPartnerSKU,
        String hsCode,
        String countryOfOrigin,
        String refCode
) {
    public VietfulProduct {
        units = List.copyOf(units);
        categories = List.copyOf(categories);
        productBundles = List.copyOf(productBundles);
        productBarcodes = List.copyOf(productBarcodes);
    }

    public record Unit(String unitCode, String unitName, BigDecimal length,
                       BigDecimal width, BigDecimal height, BigDecimal weight) { }

    public record Category(String categoryCode, String categoryName) { }

    public record Bundle(String sku, String unitCode, int qty, BigDecimal availablePercentage,
                         List<BundleDetail> bundleDetails) {
        public Bundle {
            bundleDetails = List.copyOf(bundleDetails);
        }
    }

    public record BundleDetail(int qty, BigDecimal salePrice) { }

    public record Barcode(int productId, String productName, String sku, String partnerSKU) { }
}
