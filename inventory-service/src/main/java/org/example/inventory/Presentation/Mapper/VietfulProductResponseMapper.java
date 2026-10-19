package org.example.inventory.Presentation.Mapper;

import org.example.inventory.Domain.Model.VietfulProduct;
import org.example.inventory.Presentation.Dto.Response.VietfulProductDetailResponse;
import org.example.inventory.Presentation.Dto.Response.VietfulProductListResponse;

public final class VietfulProductResponseMapper {
    private VietfulProductResponseMapper() { }

    public static VietfulProductListResponse toListItem(VietfulProduct product) {
        return new VietfulProductListResponse(
                product.productId(), product.sku(), product.partnerSKU(), product.productName(),
                product.assetType(), product.hasSerial(), product.isExpiryDate(), product.color(),
                product.size(), product.description(), product.isActive(),
                product.units().stream().map(VietfulProduct.Unit::unitCode).toList(), product.categories());
    }

    public static VietfulProductDetailResponse toDetail(VietfulProduct product) {
        return new VietfulProductDetailResponse(
                product.productId(), product.productName(), product.sku(), product.outboundType(),
                product.isExpiryDate(), product.isAttribute(), product.isLotNumber(), product.isTrackingSerial(),
                product.numDaysWarningExpDate(), product.numDaysExportBeforeExpDate(),
                product.packingNote(), product.avatarURL(), product.assetType(), product.isPhysical(),
                product.partnerSKU(), product.hasSerial(), product.color(), product.size(),
                product.description(), product.isActive(), product.units(), product.categories(),
                product.productBundles(), product.productBarcodes(), product.isDiscreet(),
                product.parentPartnerSKU(), product.hsCode(), product.countryOfOrigin(), product.refCode());
    }
}
