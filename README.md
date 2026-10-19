# kSynerX CDMS

Stores product changes from a simulated inventory service using scheduled polling and webhooks.

## 1. Start the services

Requirements: Docker Desktop (Linux containers), Docker Compose 2.24+. Run all commands from the repository root in PowerShell. Stop any Maven/IDE instances using ports 8080 or 8081.

```powershell
docker compose up -d --build --wait --wait-timeout 180
docker compose ps
```

Only three services run in Docker:

| Service | Host address |
| --- | --- |
| PostgreSQL | `localhost:5433` (database/user: `cdms`) |
| Inventory service | `http://localhost:8081` |
| CDMS | `http://localhost:8080` |

Defaults come from `.env.example`. To customize them, copy it once and edit `.env`:

```powershell
if (!(Test-Path .env)) { Copy-Item .env.example .env }
```

Webhook and inbox worker are enabled; polling is disabled by default. Re-run `docker compose up -d --wait` after changing `.env`.

## 2. Run the polling job

Enable scheduled polling from the host terminal:

```powershell
docker compose -f compose.yaml -f compose.polling.yaml up -d --wait cdms
```

The job runs inside CDMS using `@Scheduled`, Feign and `ThreadPoolTaskExecutor`: first after 5 seconds, then every 30 seconds by default.

To disable polling, keep `INVENTORY_POLLING_ENABLED=false` in `.env` and run:

```powershell
docker compose up -d --wait cdms
```

## 3. Simulate a webhook

Inventory generates **25 fake products (IDs 123–147) using Datafaker**. The callback client runs outside Docker and simulates an external sender: it fetches these snapshots, including their source revisions, and POSTs them to CDMS.

`Fake inventory data -> callback client -> CDMS webhook -> inbox_events -> worker -> products/categories`

The webhook receiving endpoint is `POST http://localhost:8080/api/v1/webhooks/inventory/products`.

With **JDK 25** installed, send all fake products from the host terminal:

```powershell
.\mvnw.cmd -f callback-client/pom.xml spring-boot:run
```

To send product 123 three times concurrently, edit `.env`, then run the same command:

```dotenv
CALLBACK_PRODUCT_ID=123
CALLBACK_REPEAT=3
CALLBACK_CONCURRENCY=3
```

Alternatively, send one fake inventory snapshot directly with PowerShell (no Java required):

```powershell
$product = Invoke-RestMethod 'http://localhost:8081/api/v1/Products/123'
$body = [System.Text.Encoding]::UTF8.GetBytes(($product | ConvertTo-Json -Depth 10))
Invoke-RestMethod `
    -Uri 'http://localhost:8080/api/v1/webhooks/inventory/products' `
    -Method Post -ContentType 'application/json; charset=utf-8' -Body $body
```

`202 Accepted` means the event was saved to the inbox. The worker then applies the snapshot or records `DUPLICATE`, `STALE` or `CONFLICT`, depending on revision and content hash. Repeating an unchanged snapshot should produce `DUPLICATE` after it has been applied.

## 4. Generate changed data

Change `INVENTORY_SEED` in `.env` (for example, to `20261021`), then run:

```powershell
docker compose up -d --wait inventory-service
```

Send the webhook again or wait for polling. Changed products receive a higher revision. Keep `inventory-service/.data` (or your configured `DOCKER_INVENTORY_DATA_DIR`) to preserve source revisions.

## 5. Inspect and stop

```powershell
docker compose logs -f cdms inventory-service
docker compose exec -T postgres psql -U cdms -d cdms -c 'SELECT status, count(*) FROM inbox_events GROUP BY status;'
docker compose exec -T postgres psql -U cdms -d cdms -c 'SELECT source_code, product_id, revision, content_hash FROM products ORDER BY product_id;'
docker compose down
```

`down` preserves the database volume and inventory snapshots. Avoid `down -v` if you need to keep the database. Fresh volumes initialize automatically; older schemas may need [the product schema migration](docker/postgres/migrations/002_simplify_products.sql).
