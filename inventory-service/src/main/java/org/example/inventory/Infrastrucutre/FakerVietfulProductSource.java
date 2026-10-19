package org.example.inventory.Infrastrucutre;

import net.datafaker.Faker;
import org.example.inventory.Domain.Interface.VietfulProductSource;
import org.example.inventory.Domain.Model.VietfulProduct;

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
        var categories = List.of(new VietfulProduct.Category("CATEGORY001", "Hàng hóa giả lập"));

        return new VietfulProduct(
                id, sku, partnerSKU, name, "Single", hasSerial, hasExpiration,
                faker.color().name(), faker.options().option("S", "M", "L", "XL", "XXL"),
                faker.lorem().sentence(), true, categories);
    }
}
