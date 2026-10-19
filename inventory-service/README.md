# Emulating VietFul — Inventory Service

Ứng dụng Spring Boot độc lập trong repository, cung cấp dữ liệu giả bằng
Datafaker 2.7.0, chỉ giữ trong RAM. Không có database, không gọi CDMS/VietFul,
không cần token hoặc profile. Các ứng dụng khác có thể gọi trực tiếp hai API
của service qua HTTP.

## Chạy service

Từ thư mục gốc repository:

```bash
./mvnw -f inventory-service/pom.xml spring-boot:run
```

Mặc định chạy tại `http://localhost:8081`:

```bash
curl -sS http://localhost:8081/api/v1/Products
curl -sS http://localhost:8081/api/v1/Products/123
```

Không cần chạy CDMS hoặc PostgreSQL. Đường dẫn có chữ **P** viết hoa và detail
dùng **productId dạng số**, không dùng partnerSKU.

Đổi cổng bằng biến môi trường (Spring Boot không tự đọc `.env`):

```bash
INVENTORY_PORT=8091 ./mvnw -f inventory-service/pom.xml spring-boot:run
```

## Contract và dữ liệu

- `GET /api/v1/Products`: JSON array trực tiếp, không bọc envelope.
- `GET /api/v1/Products/{productId}`: JSON object chi tiết theo mẫu đã cung cấp.
- 25 sản phẩm, ID **123–147**, SKU/partnerSKU duy nhất; list/detail nhất quán.
- `units` trong list là chuỗi mã; trong detail là object kích thước/trọng lượng.
- Giữ đầy đủ `categories`, `productBundles/bundleDetails`, `productBarcodes`
  và các key `partnerSKU`, `avatarURL`, `isActive`…
- Faker sinh dữ liệu một lần khi khởi động, seed `20261019`; dữ liệu bất biến
  trong RAM, không sinh mới mỗi request. Cùng code/phiên bản sẽ tái tạo cùng dữ liệu.
- Không ghi database/file, chưa có phân trang, API ghi hoặc đồng bộ thay đổi.
- URL ảnh `example.com` chỉ là chuỗi minh họa.
- ID không tồn tại: **404**; ID không phải số nguyên int: **400**; POST: **405**.
  Lỗi trả dạng `{ "code": "...", "message": "..." }`.

## Cấu trúc riêng

```text
inventory-service/
├── pom.xml
└── src/main/
    ├── java/org/example/inventory/
    │   ├── InventoryApplication.java
    │   ├── Domain/            # Model và cổng đọc sản phẩm
    │   ├── Application/       # Service, use case
    │   ├── Infrastrucutre/    # Faker, cấu hình bean
    │   └── Presentation/     # Controller, DTO, mapper, xử lý lỗi
    └── resources/application.yaml
```

Service có `pom.xml`, namespace và entry point riêng, không phụ thuộc jar/CDMS,
không kế thừa pom ở thư mục gốc. Có thể import riêng `inventory-service/pom.xml`
vào IDE hoặc build/run bằng Maven khi đứng trong thư mục service.

## Build riêng

Yêu cầu Java 25; lần đầu Maven cần internet để tải dependency.

```bash
# Từ thư mục gốc repository
./mvnw -f inventory-service/pom.xml clean package -DskipTests
java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar
```

Hoặc chạy `mvn clean package` trong thư mục `inventory-service/` nếu đã cài Maven.
Không tạo lại file test theo yêu cầu. Phần PostgreSQL/Docker và API mẫu cũ của
CDMS được giữ nguyên, không liên quan tới service này.
