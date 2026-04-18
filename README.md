# 🪑 MMD Furniture House — Management System

A complete, production-ready **Spring Boot 3** application for managing a furniture shop — billing, inventory, quotations, PDF export, and more.

---

## ✨ Features

| Feature | Details |
|---|---|
| **Quotations** | Create quotes without deducting stock |
| **Final Bills** | One-click convert quotation → bill (deducts stock) |
| **PDF Export** | Professional GST-compliant PDFs (Flying Saucer) |
| **Inventory** | Add/edit products, add stock, low-stock alerts |
| **Customers** | B2B/B2C customer management with GST support |
| **Dashboard** | Revenue, bills, quotations, low-stock widgets |
| **Security** | Session-based login with BCrypt password hashing |
| **H2 File DB** | Persistent file-mode H2 — no external DB needed |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run
```bash
cd furniture-shop-management
mvn spring-boot:run
```

### Access
| URL | Description |
|---|---|
| http://localhost:8080 | Main application |
| http://localhost:8080/login | Login page |
| http://localhost:8080/h2-console | H2 Database Console |

### Default Credentials
| Username | Password |
|---|---|
| `admin` | `admin123` |

---

## 🗂️ Project Structure

```
src/main/java/com/furnitureshop/
├── FurnitureShopApplication.java   # Entry point
├── config/
│   ├── SecurityConfig.java         # Spring Security (form login, session)
│   ├── ShopConfig.java             # Shop branding from application.properties
│   └── WebConfig.java              # MVC configuration
├── controller/
│   ├── AuthController.java         # Login / Profile / Password change
│   ├── DashboardController.java    # Dashboard stats
│   ├── InvoiceController.java      # Billing (create, view, convert, cancel, PDF)
│   ├── ProductController.java      # Inventory CRUD + stock management
│   └── CustomerController.java     # Customer CRUD
├── dto/
│   ├── InvoiceFormDto.java
│   ├── InvoiceItemDto.java
│   └── DashboardStatsDto.java
├── model/
│   ├── AppUser.java                # Authenticated users
│   ├── Product.java                # Furniture products with HSN codes
│   ├── Customer.java               # Customers (B2B/B2C)
│   ├── Invoice.java                # Quotation or Final Bill
│   ├── InvoiceItem.java            # Line items with soldAtPrice snapshot
│   └── InvoiceStatus.java          # QUOTATION / FINAL_BILL / CANCELLED
├── repository/                     # Spring Data JPA repositories
├── security/
│   └── CustomUserDetailsService.java
└── service/
    ├── ProductService.java
    ├── CustomerService.java
    ├── InvoiceService.java         # Core business logic + stock deduction
    ├── PdfService.java             # Flying Saucer PDF generation
    └── UserService.java

src/main/resources/
├── application.properties          # All config (shop name, DB, etc.)
├── db/data.sql                     # Seed data (admin user + sample products)
└── templates/
    ├── common/layout.html          # Thymeleaf layout with sidebar
    ├── auth/login.html             # Login page
    ├── auth/profile.html           # Profile + password change
    ├── dashboard.html              # Main dashboard
    ├── billing/
    │   ├── new-bill.html           # Create bill/quotation (Alpine.js live calc)
    │   ├── invoice-view.html       # View invoice with convert/cancel/PDF
    │   ├── invoice-list.html       # Searchable list (quotations & history)
    │   ├── customers.html          # Customer list
    │   └── customer-form.html      # Add/edit customer
    └── inventory/
        ├── list.html               # Product grid with inline stock update
        └── form.html               # Add/edit product
```

---

## 🔧 Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Shop Branding (appears on all PDFs)
shop.name=MMD Furniture House
shop.address=123, MG Road, Bengaluru - 560001, Karnataka
shop.phone=+91-9876543210
shop.email=info@MMDfurniturehouse.com
shop.gstin=29AABCR1234F1Z5
shop.tagline=Quality Furniture for Every Home

# Database (file-persistent H2)
spring.datasource.url=jdbc:h2:file:./data/furnituredb
spring.datasource.username=admin
spring.datasource.password=furniture@2024
```

---

## 💡 Key Business Rules

### Quotation vs Final Bill
- **Quotation**: Created without stock deduction. Can be converted or cancelled.
- **Final Bill**: Stock is deducted immediately (either on creation or on conversion from quotation).
- **Conversion**: One-click button in the UI. Backend validates stock availability before proceeding.
- **Price Snapshot**: `InvoiceItem.soldAtPrice` records the price at transaction time — historical bills are never affected by future price changes.

### GST Calculation
- All items attract **18% GST** (9% CGST + 9% SGST)
- Calculated automatically on subtotal
- Itemised on PDFs with HSN codes

### Invoice Numbering
- Quotations: `QUO/YYMM/XXXX`
- Bills: `BILL/YYMM/XXXX`
- Converted quotations get a new bill number

---

## 📄 PDF Generation

PDFs are generated using **Flying Saucer (iText)** from XHTML templates.

Every PDF includes:
- Shop branding (name, address, GSTIN, contact)
- Customer details (name, phone, address, GST if applicable)
- Itemised table with HSN codes, quantities, unit prices
- CGST 9% / SGST 9% breakdown
- Grand total
- Watermark on quotations
- Footer with legal notice

Download via: `GET /billing/pdf/{invoiceId}`

---

## 🔒 Security

- Spring Security session-based authentication
- BCrypt password hashing (strength 12)
- CSRF protection on all forms
- Session timeout: 60 minutes
- All endpoints protected — unauthenticated users redirected to `/login`
- H2 console restricted to ROLE_ADMIN

---

## 🛠️ Build for Production

```bash
mvn clean package -DskipTests
java -jar target/furniture-shop-management-1.0.0.jar
```

The H2 database file will be created at `./data/furnituredb.mv.db` — back this up regularly.

---

## 📦 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security 6 |
| Frontend | Thymeleaf + Tailwind CSS (CDN) + Alpine.js |
| PDF | Flying Saucer 9.1.22 (iText) |
| Database | H2 (file-persistent mode) |
| Build | Maven |

---

## 🤝 Customisation Tips

1. **Change shop details** → edit `application.properties`
2. **Add more users** → Use `UserService.createUser()` or a new admin panel
3. **Change GST rate** → Edit `Invoice.CGST_RATE` / `Invoice.SGST_RATE` and `PdfService`
4. **Switch to MySQL/PostgreSQL** → Change datasource URL + add driver dependency
5. **Add email on bill** → Inject `JavaMailSender` into `InvoiceService`

---

*Built with ❤️ using Spring Boot 3, Thymeleaf, Tailwind CSS, and Flying Saucer PDF*
