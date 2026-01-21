# Hung Hypebeast – E-commerce Backend (Phase 1)

## Tech
- Java 21, Spring Boot 3.x
- PostgreSQL 15
- Flyway (migrations + seed)
- Spring Security (HTTP Basic cho Admin)
- Spring Mail (có thể cấu hình; mặc định NOOP log)

## Quick start (Docker)
```bash
docker compose up --build
```

API: http://localhost:8080  
DB: localhost:5432 (postgres/postgres), database: assignment_2

## Quick start (Local)
1) Start PostgreSQL & create DB `assignment_2`
2) Run app
```bash
mvn spring-boot:run
```

## Public APIs

### Catalog
- `GET /api/public/products?page=1&size=20&category=ao-thun&minPrice=100000&maxPrice=500000&keyword=rong`
- `GET /api/public/products/{slug}`

### Cart
- `POST /api/public/carts` -> tạo cart token
- `GET /api/public/carts/{cartToken}`
- `POST /api/public/carts/{cartToken}/items` body: `{ "variantId": "...", "quantity": 1 }`
- `PUT /api/public/carts/{cartToken}/items/{variantId}` body: `{ "quantity": 2 }`
- `DELETE /api/public/carts/{cartToken}/items/{variantId}`

### Inventory Reservation (Hold 10–15 phút)
- `POST /api/public/checkout/reserve` body: `{ "cartToken": "...", "holdMinutes": 15 }`
    - trả về `reservationToken` + `expiresAt`
- `POST /api/public/checkout/reserve/{reservationToken}/cancel`

### Place Order (Checkout)
- `POST /api/public/checkout/orders`
```json
{
  "reservationToken": "uuid",
  "email": "customer@example.com",
  "fullName": "Nguyen Van A",
  "phone": "0900000000",
  "addressLine1": "12 Nguyen Trai",
  "addressLine2": "",
  "city": "HCM",
  "paymentMethod": "COD"
}
```
Response: `{ "orderCode": "HHB-YYYYMMDD-XXXXXX", "trackingToken": "uuid" }`

### Tracking (không cần login)
- `GET /api/public/orders/track/{trackingToken}`

### SePay webhook (Phase 1 stub)
- `POST /api/public/payments/sepay/webhook`
```json
{ "orderCode": "HHB-..." }
```
-> đổi trạng thái đơn sang `PAID`

## Admin APIs (requires Basic Auth)
- `GET /api/admin/orders?page=1&size=20`
- `PUT /api/admin/orders/{orderId}/status` body: `{ "status": "SHIPPING" }`

## Notes (Inventory correctness)
- Khi reserve: dùng DB pessimistic lock trên `product_variants` để đảm bảo *last item* không bị oversell.
- Reservation hết hạn sẽ tự nhả bằng scheduler (mặc định chạy mỗi 30s).
