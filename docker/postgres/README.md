# CDMS PostgreSQL

Database triển khai toàn bộ schema DBML đã cung cấp: 8 bảng, 2 enum,
6 khóa ngoại, 12 check constraints, 7 unique constraints và 4 index tra cứu.
Các bảng nằm trong namespace PostgreSQL `public`; `TableGroup` trong DBML chỉ
là nhóm hiển thị, không phải các namespace riêng.

## Chạy database

Tại thư mục gốc dự án:

```bash
docker compose up -d --wait postgres
docker compose ps
```

Thông tin kết nối mặc định dùng cho phát triển local:

| Thuộc tính | Giá trị |
| --- | --- |
| Host | `localhost` |
| Port từ máy host | `5433` |
| Database | `cdms` |
| User | `cdms` |
| Password local | `cdms_local_password` |
| JDBC URL | `jdbc:postgresql://localhost:5433/cdms` |
| Địa chỉ trong Compose network | `postgres:5432` |

Cổng 5433 tránh PostgreSQL của dự án khác đang chạy trên cổng 5432.
Container chỉ publish trên loopback `127.0.0.1`. Image dùng `postgres:17-alpine`,
volume riêng `ksynerx-cdms_postgres_data` giữ dữ liệu qua các lần khởi động.
Đây là tài khoản quản trị database cho môi trường local, không phải cấu hình production.

Có thể sao chép `.env.example` thành `.env` và chỉnh thông tin trước lần chạy đầu.
Compose đọc `.env` tự động; Spring Boot cần các biến tương ứng được export
trong terminal hoặc đặt trong Run Configuration của IDE. Đổi user/password
trong `.env` không tự thay đổi tài khoản trong một volume đã khởi tạo.

```bash
docker compose exec postgres sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

Trong `psql`, dùng `\dt` để xem bảng, `\d products` để xem cấu trúc,
`\dT+` để xem enum và `\di` để xem index.

## Các bảng và quan hệ

| Nhóm | Bảng | Vai trò |
| --- | --- | --- |
| ProductData | `products` | Snapshot mới nhất theo `(source_code, product_id)` |
| ProductData | `product_units` | Đơn vị, kích thước và trọng lượng |
| ProductData | `product_categories` | Category snapshot riêng cho từng sản phẩm |
| ProductData | `product_bundles` | Bundle và thông tin số lượng |
| ProductData | `product_bundle_details` | Chi tiết con của bundle |
| ProductData | `product_barcodes` | Barcode snapshot của sản phẩm |
| ChannelConfiguration | `channels` | Một cấu hình cho mỗi POLLING/WEBHOOK/EXCEL |
| EventProcessing | `inbox_events` | Sự kiện nhận vào, retry và kết quả xử lý |

Xóa sản phẩm sẽ cascade tới các bảng con, bao gồm chi tiết bundle.
Không thể xóa channel khi còn inbox event tham chiếu tới channel đó.
`inbox_events.product_id`, `barcode_product_id`, `parent_partner_sku` không có
FK theo đúng schema: dữ liệu bên ngoài có thể đến trước khi sản phẩm được ingest.
Mỗi array lưu thứ tự zero-based qua `position`, không cho trùng vị trí trong cùng cha.

Enum `channel_type`: `POLLING`, `WEBHOOK`, `EXCEL`.
Enum `inbox_status`: `PENDING`, `RETRY`, `APPLIED`, `STALE`, `DUPLICATE`, `CONFLICT`, `FAILED`.

Không seed dữ liệu nghiệp vụ hoặc channel tự động. `products` và inbox cần revision
và content hash hợp lệ từ pipeline ingest thực tế.

## Khởi tạo và nâng cấp schema

`init/001_cdms_schema.sql` chạy trong một transaction khi volume còn trống.
Script giữ tên index/check constraint và kiểu dữ liệu từ DBML, sử dụng identity
cho ID tự tăng, `numeric` không giới hạn precision và `timestamptz` cho thời gian.
Các ghi chú nghiệp vụ quan trọng được lưu thành `COMMENT ON` để xem từ DB client.

Script init **chỉ chạy khi database được khởi tạo lần đầu**. Restart container hoặc
sửa SQL rồi chạy `up` không cập nhật schema của volume cũ. Các thay đổi sau này cần
migration riêng; không chạy lại script tạo schema lên database đã có bảng và không
xóa volume để nâng cấp dữ liệu đang sử dụng.

`docker compose stop postgres` dừng database; `docker compose start postgres` khởi
động lại. `docker compose down` giữ named volume. Không thêm `-v` nếu muốn giữ dữ liệu.

Nguồn: [PostgreSQL Docker Official Image – Initialization scripts](https://hub.docker.com/_/postgres).

Các quy tắc trong Note cần thực hiện ở application: canonical hash và chính sách
null/empty collection thống nhất, revision từ nguồn, source_code bất biến khi đã có
event, poll interval null cho WEBHOOK/EXCEL, complete snapshot validation và cập nhật
`updated_at` tường minh. Không tự thêm trigger hay constraint ngoài DBML cho các quy tắc này.
Worker cần giữ khóa `FOR UPDATE SKIP LOCKED` và ghi product, các bảng con, kết quả
inbox trong cùng transaction. DDL không tự triển khai worker hoặc cơ chế idempotency.

## Kiểm tra schema thật trên PostgreSQL

```bash
bash docker/postgres/verify.sh
```

Lệnh tạo database tạm tên `cdms_verify_*`, nạp DDL, kiểm tra các ràng buộc và xóa
đúng database tạm sau khi xong. Không ghi dữ liệu test vào database ứng dụng `cdms`.
Kiểm tra bao gồm unique key, revision/hash, JSON object, vị trí array, cascade nhiều
cấp, restrict channel deletion, default values và cho phép inbox delivery lặp lại.

## Kết nối ứng dụng và phạm vi hiện tại

`application.yaml` mặc định dùng PostgreSQL, driver là `org.postgresql.Driver`,
`spring.sql.init.mode=never`: Docker chịu trách nhiệm khởi tạo schema.
Đổi host/URL bằng `DB_URL`; database/user/password/port bằng `POSTGRES_*`.

**API sản phẩm mẫu hiện tại chưa được chuyển sang mô hình CDMS.** Repository cũ
vẫn query `name`, `price` và `product_id` dạng chuỗi; schema mới dùng `product_name`,
`product_id BIGINT` và khóa `(source_code, product_id)`, không có cột `price` ở
`products`. Vì vậy gọi POST sản phẩm mẫu trên database mới sẽ lỗi cho đến khi cập
nhật model, DTO và query. Không tạo cột giả hoặc view tương thích làm lệch schema.

H2 chỉ còn dependency scope `test`. Các test API mẫu bật profile `legacy-test`,
dùng schema/fixtures dưới `src/test/resources/legacy`; chúng không chứng minh API
đã tích hợp CDMS. Kiểm thử database CDMS độc lập bằng `verify.sh` ở trên.

```bash
./mvnw test
```
