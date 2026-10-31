# BPMN Workflow Engine for Logistics

Hệ thống quản lý và thực thi quy trình nghiệp vụ dành cho các doanh nghiệp Logistics theo mô hình Multi-tenant SaaS.

## 1. Giới thiệu

Dự án hướng đến xây dựng một nền tảng cho phép nhiều doanh nghiệp Logistics cùng sử dụng hệ thống để:

- Quản lý quy trình nghiệp vụ.
- Cấu hình quy trình từ các bước và trạng thái có giới hạn.
- Theo dõi tiến độ thực hiện quy trình.
- Quản lý đơn hàng, nhiệm vụ và nhân viên phụ trách.
- Lưu trữ lịch sử xử lý.
- Đảm bảo dữ liệu giữa các doanh nghiệp được phân tách.

### Quy trình Logistics minh họa

Tiếp nhận hàng → Kiểm tra → Phân loại → Lưu kho → Điều phối → Giao hàng → Hoàn tất.

## 2. Kiến trúc hệ thống

- **Kiến trúc ứng dụng:** Modular Monolith.
- **Mô hình triển khai:** Multi-tenant SaaS.
- **Phân tách dữ liệu:** Shared Database / Shared Schema.
- **Cơ chế cô lập dữ liệu:** tenant_id.
- **Workflow Engine:** Custom Sequential State Machine.

## 3. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17, Spring Boot 4 |
| ORM | Spring Data JPA, Hibernate |
| Database | PostgreSQL 15 |
| Database Migration | Flyway |
| Frontend | React, Vite (dự kiến) |
| Container | Docker, Docker Compose |
| Version Control | Git, GitHub |

## 4. Cấu trúc dự án

```text
bpmn_workflow_engine/
├── backend/
├── frontend/
├── database/
├── docker/
├── docs/
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md