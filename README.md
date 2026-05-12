# 🛒 Scalable E-Commerce Platform (Spring Boot & Next.js)

## 📖 Tổng quan dự án

Dự án này là một hệ thống thương mại điện tử được thiết kế với kiến trúc linh hoạt, cho phép mở rộng dễ dàng từ một khối nguyên khối (**Monolith**) trong giai đoạn MVP (*Minimum Viable Product*) lên kiến trúc **Microservices** khi nền tảng phát triển.

Hệ thống áp dụng triệt để phương pháp **Domain-Driven Design (DDD)** để tổ chức mã nguồn độc lập, rõ ràng giữa các phân hệ.

---

# 🛠️ Stack Công nghệ

## Frontend

- **Next.js 14 (App Router)**
  - Tối ưu SEO
  - Hỗ trợ SSR/SSG linh hoạt
  - Streaming UI hiện đại

## Backend

- **Java Spring Boot 3.x**
- **Spring Data JPA**
- **Hibernate**
- **Spring Security**
- **JWT Authentication**

Spring Boot mang lại hiệu năng xử lý đa luồng tốt cho các hệ thống thương mại điện tử có traffic lớn hoặc biến động nhanh.

## Database

- **PostgreSQL**
  - Kiến trúc EAV mạnh mẽ
  - Quan hệ dữ liệu linh hoạt
  - Hỗ trợ mở rộng tốt

## Migration

- **Flyway**
  - Quản lý version database
  - Migration bằng SQL thuần

## Caching

- **Redis**
  - Cart management
  - Rate limiting
  - Session caching

## Utility Libraries

- **MapStruct**
  - Tự động mapping DTO ↔ Entity

---

# ✨ Tính năng cốt lõi

## 🧩 1. Sản phẩm đa hình (EAV Pattern)

Database được thiết kế tách biệt giữa:

- Thông tin chung của sản phẩm (`product`)
- Biến thể có giá/tồn kho (`sku`)

Kết hợp với bảng `attribute_template` gắn theo danh mục, hệ thống có thể dễ dàng cấu hình và bán đa dạng loại sản phẩm:

- Điện tử
- Thời trang
- Thể thao
- Gia dụng
- Mỹ phẩm

mà không cần thay đổi schema lõi.

### Ví dụ cấu trúc

```text
Product
 ├── SKU 1
 │    ├── RAM: 16GB
 │    ├── Color: Black
 │    └── Price: 25.000.000
 │
 └── SKU 2
      ├── RAM: 32GB
      ├── Color: Silver
      └── Price: 30.000.000
```

---

## ⚡ 2. Giỏ hàng hiệu năng cao

Dữ liệu giỏ hàng được quản lý bằng **Redis Hash**:

- Mỗi user = 1 key
- Mỗi SKU = 1 field

Ví dụ:

```text
cart:user:12
 ├── sku:101 → quantity:2
 ├── sku:205 → quantity:1
 └── sku:300 → quantity:5
```

### Lợi ích

- Truy xuất realtime
- Tốc độ cực nhanh
- Giảm tải PostgreSQL
- Scale tốt khi traffic lớn

---

## 🧾 3. Order Snapshot

Hệ thống lưu snapshot dữ liệu vào `order_item` ngay khi đặt hàng:

- Tên sản phẩm
- Tên SKU
- Giá tại thời điểm mua
- Thông số sản phẩm

Điều này đảm bảo lịch sử giao dịch luôn chính xác ngay cả khi:

- Giá sản phẩm thay đổi
- SKU bị xóa
- Product bị cập nhật

### Ví dụ

```sql
order_item
-----------------------------------
product_name  = "iPhone 15"
sku_name      = "Black - 256GB"
unit_price    = 28990000
quantity      = 1
```

---

## 🔐 4. Authentication & Authorization

### Authentication

- JWT Access Token
- JWT Filter
- Stateless Security

### Authorization

Role-based access control:

| Role | Quyền |
|---|---|
| ADMIN | Quản lý sản phẩm |
| CUSTOMER | Đặt hàng |

---

# 📁 Cấu trúc Dự án

```bash
src/main/java/com/yourshop/
│
├── product/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── mapper/
│
├── order/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── user/
│   ├── controller/
│   ├── service/
│   ├── security/
│   └── entity/
│
├── payment/
│   ├── vnpay/
│   ├── momo/
│   └── service/
│
└── common/
    ├── config/
    ├── security/
    ├── exception/
    ├── util/
    └── response/
```

---

# 🚀 Hướng dẫn thiết lập cơ bản

## 1️⃣ Clone project

```bash
git clone https://github.com/your-username/your-project.git
```

---

## 2️⃣ Cấu hình Environment Variables

Tạo file:

```bash
.env
```

Ví dụ:

```env
DB_URL=jdbc:postgresql://localhost:5432/shopdb
DB_USERNAME=postgres
DB_PASSWORD=123456

JWT_SECRET=your-secret-key

REDIS_HOST=localhost
REDIS_PORT=6379
```

### ⚠️ Lưu ý bảo mật

Bắt buộc thêm vào `.gitignore`:

```gitignore
.env
application-prod.yml
```

---

## 3️⃣ Khởi tạo Database

Flyway sẽ tự động chạy các migration:

```text
V1__init.sql
V2__create_product.sql
V3__create_order.sql
...
```

### ⚠️ Không sử dụng

```yaml
spring.jpa.hibernate.ddl-auto=create
```

trong production để tránh mất dữ liệu.

---

## 4️⃣ Khởi động Redis

### Docker

```bash
docker run -d -p 6379:6379 redis
```

---

## 5️⃣ Run Backend

```bash
./mvnw spring-boot:run
```

---

## 6️⃣ Run Frontend

```bash
npm install
npm run dev
```

---

# ☁️ Triển khai hệ thống

## Development / Staging

Có thể sử dụng:

- Render
- Railway

### Yêu cầu tối thiểu

- RAM: 512MB

---

## Production

Khuyến nghị:

- VPS 1GB RAM trở lên

### Nhà cung cấp phù hợp

- DigitalOcean
- Vultr
- Hetzner

---

# 🔮 Roadmap phát triển

## 🚀 Giai đoạn 1 — MVP

### Mục tiêu

- Go-to-market nhanh
- Monolith Architecture
- Tập trung core business

### Thời gian

- 1 → 3 tháng

---

## ⚙️ Giai đoạn 2 — Scale System

Tách dần thành:

- Product Service
- Order Service
- User Service

### Công nghệ

- Docker
- API Gateway
- Message Queue

---

## ☸️ Giai đoạn 3 — Enterprise Scale

### Tích hợp

- Kubernetes
- CDN
- Typesense Search Engine
- Distributed Cache
- CI/CD Pipeline

---

# 🤖 Tích hợp AI Assistant (RAG Architecture)

## 🧠 Vector Search

Cài extension:

```sql
CREATE EXTENSION vector;
```

Sử dụng `pgvector` để lưu embedding của:

- Product description
- SKU attributes
- Search keywords

---

## 🔗 AI Backend

### Công nghệ

- Spring AI
- LangChain4j

### Kết nối LLM

- OpenAI
- Gemini

### Workflow

```text
User Question
      ↓
Embedding
      ↓
Vector Search
      ↓
Retrieve Context
      ↓
LLM Response
```

---

## 💬 AI Chatbot Frontend

Sử dụng:

- Vercel AI SDK

### Tính năng

- Streaming response
- Realtime typing
- AI shopping assistant

---

## 🛒 Function Calling

AI có thể:

- Hiểu intent người dùng
- Gọi API backend
- Thêm SKU trực tiếp vào Redis Cart

### Ví dụ

```text
"Thêm giúp tôi áo Nike màu đen size L"
```

AI sẽ:

1. Search SKU phù hợp
2. Gọi API add-to-cart
3. Trả kết quả cho user

---

# 📌 Định hướng kiến trúc

Dự án được xây dựng theo triết lý:

- Scalable First
- Maintainable
- Domain-Oriented
- Cloud Ready
- AI Ready

Mục tiêu là xây dựng một nền tảng thương mại điện tử hiện đại có khả năng mở rộng lâu dài thay vì chỉ phục vụ MVP ngắn hạn.

---

# 📄 License

MIT License
