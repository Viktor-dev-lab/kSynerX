package org.example.inventory.Infrastrucutre;

import net.datafaker.Faker;
import org.example.inventory.Domain.Interface.VietfulProductSource;
import org.example.inventory.Domain.Model.VietfulProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

public class FakerVietfulProductSource implements VietfulProductSource {
    private static final int FIRST_PRODUCT_ID = 123;
    private static final int PRODUCT_COUNT = 25;
    private static final long SEED = 20261019L;

    private final List<VietfulProduct> products;

    public FakerVietfulProductSource() {
        Faker faker = new Faker(Locale.ENGLISH, new Random(SEED));
        products = IntStream.range(FIRST_PRODUCT_ID, FIRST_PRODUCT_ID + PRODUCT_COUNT)
                .mapToObj(id -> createProduct(faker, id))
                .toList();
    }

    @Override
    public List<VietfulProduct> getProducts() {
        return products;
    }

    @Override
    public Optional<VietfulProduct> getProduct(int productId) {
        return products.stream().filter(product -> product.productId() == productId).findFirst();
    }

    private VietfulProduct createProduct(Faker faker, int id) {
        String name = faker.commerce().productName();
        String sku = "SKU-" + id;
        String partnerSKU = "PARTNER-" + id;
        boolean hasSerial = faker.bool().bool();
        boolean hasExpiration = faker.bool().bool();
        var units = List.of(new VietfulProduct.Unit("CAI", "Cái",
                dimension(faker), dimension(faker), dimension(faker), dimension(faker)));
        var categories = List.of(new VietfulProduct.Category("CATEGORY001", "Hàng hóa giả lập"));
        var bundleDetails = List.of(
                new VietfulProduct.BundleDetail(faker.number().numberBetween(1, 10), price(faker)),
                new VietfulProduct.BundleDetail(faker.number().numberBetween(10, 20), price(faker)));
        var bundles = List.of(new VietfulProduct.Bundle(sku, "CAI",
                faker.number().numberBetween(1, 20),
                BigDecimal.valueOf(faker.number().numberBetween(1, 101), 2), bundleDetails));
        var barcodes = List.of(new VietfulProduct.Barcode(id, name, sku, partnerSKU));

        return new VietfulProduct(
                id, name, sku, "NotDefined", hasExpiration, false, hasExpiration, hasSerial,
                hasExpiration ? 30 : 0, hasExpiration ? 7 : 0,
                faker.lorem().sentence(), "https://example.com/products/" + id + ".jpg",
                "Single", true, partnerSKU, hasSerial,
                faker.color().name(), faker.options().option("S", "M", "L", "XL", "XXL"),
                faker.lorem().sentence(), true, units, categories, bundles, barcodes,
                faker.bool().bool(), "", faker.numerify("########"),
                faker.options().option("VN", "TH", "US", "JP"), "REF-" + id);
    }

    private BigDecimal dimension(Faker faker) {
        return BigDecimal.valueOf(faker.number().numberBetween(1, 101), 2);
    }

    private BigDecimal price(Faker faker) {
        return BigDecimal.valueOf(faker.number().numberBetween(10000, 1000000), 2);
    }
}
