# Inventory Service - Mikroservis Mimarisi

Spring Boot ve Docker kullanılarak geliştirilmiş, sipariş ve envanter yönetimi için mikroservis tabanlı bir uygulama.

## 📋 İçindekiler

- [Proje Hakkında]
- [Mimari]
- [Teknolojiler]
- [Özellikler]
- [Kurulum]
- [API Kullanımı]
- [Docker ile Çalıştırma]

## 🎯 Proje Hakkında

Bu proje, mikroservis mimarisinin temel prensiplerini gösteren bir e-ticaret uygulamasıdır. İki ana mikroservis içerir:

- **Order Service**: Sipariş oluşturma ve yönetme
- **Inventory Service**: Ürün stoklarını kontrol etme

Servisler arasında senkron iletişim (REST API) kullanılmaktadır.

## 🏗️ Mimari

```
┌─────────────────────┐      REST API       ┌──────────────────────┐
│   Order Service     │ ─────────────────► │  Inventory Service   │
│   (Port: 8082)      │    WebClient        │    (Port: 8081)      │
└─────────────────────┘                     └──────────────────────┘
         │                                              │
         │                                              │
         ▼                                              ▼
   ┌──────────┐                                ┌──────────────┐
   │  H2 DB   │                                │ PostgreSQL   │
   └──────────┘                                └──────────────┘
```

### Servisler

1. **Inventory Service**
   - Port: `8081`
   - Veritabanı: PostgreSQL
   - Sorumluluk: Ürün stoklarını yönetir ve stok durumunu kontrol eder

2. **Order Service**
   - Port: `8082`
   - Veritabanı: H2 (In-Memory)
   - Sorumluluk: Siparişleri oluşturur ve listeler
   - Inventory Service ile iletişim kurar

3. **PostgreSQL Database**
   - Port: `5432`
   - Veritabanı: `inventorydb`
   - Kullanıcı: `postgres`

## 🛠️ Teknolojiler

### Backend
- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Data JPA**
- **Spring WebFlux** (WebClient için)
- **Spring Cloud OpenFeign** (Order Service'te)
- **Lombok**

### Veritabanı
- **PostgreSQL 15** (Inventory Service)
- **H2 Database** (Order Service)

### DevOps
- **Docker & Docker Compose**
- **Maven**

## ✨ Özellikler

### Order Service
- ✅ Yeni sipariş oluşturma
- ✅ Tüm siparişleri listeleme
- ✅ Stok kontrolü ile entegre sipariş yönetimi
- ✅ WebClient ile senkron iletişim
- ✅ Transaction yönetimi

### Inventory Service
- ✅ Ürün stok durumu sorgulama
- ✅ Çoklu ürün kontrolü (Bulk query)
- ✅ PostgreSQL ile kalıcı veri saklama

## 🚀 Kurulum

### Gereksinimler

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Yerel Kurulum (Manuel)

1. **Projeyi klonlayın**
```bash
git clone https://github.com/eminortaca/Inventory-Service.git
cd Inventory-Service
```

2. **PostgreSQL'i başlatın**
```bash
docker run -d \
  --name inventory-postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=inventorydb \
  -p 5432:5432 \
  postgres:15
```

3. **Inventory Service'i başlatın**
```bash
cd inventory-service
./mvnw clean install
./mvnw spring-boot:run
```

4. **Order Service'i başlatın**
```bash
cd ../order-service
./mvnw clean install
./mvnw spring-boot:run
```

## 🐳 Docker ile Çalıştırma

### Docker Compose ile Tüm Sistemi Başlatma

```bash
# Projeyi klonlayın
git clone https://github.com/eminortaca/Inventory-Service.git
cd Inventory-Service

# Tüm servisleri başlatın
docker-compose up --build
```

Servisler şu portlarda çalışacaktır:
- **Order Service**: http://localhost:8082
- **Inventory Service**: http://localhost:8081
- **PostgreSQL**: localhost:5432

### Servisleri Durdurmak

```bash
docker-compose down
```

### Tüm Verileri Silmek

```bash
docker-compose down -v
```

## 📡 API Kullanımı

### Inventory Service

#### Stok Durumunu Kontrol Et
```http
GET http://localhost:8081/api/inventory?skuCode=iphone_13&skuCode=samsung_s21
```

**Response:**
```json
[
  {
    "skuCode": "iphone_13",
    "inStock": true
  },
  {
    "skuCode": "samsung_s21",
    "inStock": false
  }
]
```

### Order Service

#### Yeni Sipariş Oluştur
```http
POST http://localhost:8082/api/order
Content-Type: application/json

{
  "orderLineItemsDtoList": [
    {
      "skuCode": "iphone_13",
      "price": 1200.00,
      "quantity": 2
    },
    {
      "skuCode": "samsung_s21",
      "price": 900.00,
      "quantity": 1
    }
  ]
}
```

**Response (Success):**
```text
Sipariş başarıyla oluşturuldu
```

**Response (Stok Yok):**
```text
Ürün stokta yok, lütfen daha sonra tekrar deneyiniz.
```

#### Tüm Siparişleri Listele
```http
GET http://localhost:8082/api/order
```

**Response:**
```json
[
  {
    "id": 1,
    "orderNumber": "550e8400-e29b-41d4-a716-446655440000",
    "orderLineItemsList": [
      {
        "id": 1,
        "skuCode": "iphone_13",
        "price": 1200.00,
        "quantity": 2
      }
    ]
  }
]
```

## 🔧 Yapılandırma

### Inventory Service - application.properties
```properties
spring.application.name=inventory-service
spring.datasource.url=jdbc:postgresql://inventory-db:5432/inventorydb
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

### Order Service - application.properties
```properties
spring.application.name=order-service
server.port=8081
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

## 📦 Proje Yapısı

```
Inventory-Service/
├── inventory-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/inventoryservice/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       └── dto/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── order-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/order_service/
│   │   │   │       ├── controller/
│   │   │   │       ├── services/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       ├── dto/
│   │   │   │       ├── client/
│   │   │   │       └── config/
│   │   │   └── resources/
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml
└── README.md
```

## 🧪 Test

### Unit Testleri Çalıştırma

```bash
# Inventory Service testleri
cd inventory-service
./mvnw test

# Order Service testleri
cd order-service
./mvnw test
```

### Postman ile Manuel Test

1. Postman'i açın
2. Yukarıdaki API örneklerini kullanarak request'leri gönderin
3. Response'ları kontrol edin

## 🐛 Troubleshooting

### Ortak Sorunlar

**Problem:** Port zaten kullanılıyor
```bash
# Çözüm: İlgili portu kullanan container'ı durdurun
docker ps
docker stop <container-id>
```

**Problem:** PostgreSQL bağlantı hatası
```bash
# Çözüm: PostgreSQL container'ının çalıştığından emin olun
docker-compose logs inventory-db
```

**Problem:** Order Service, Inventory Service'e ulaşamıyor
- Docker network ayarlarını kontrol edin
- Her iki servisin de aynı network'te olduğundan emin olun

## 📝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'feat: Add amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 👤 İletişim

**Emin Ortaca** - [@eminortaca](https://github.com/eminortaca)

Proje Linki: [https://github.com/eminortaca/Inventory-Service](https://github.com/eminortaca/Inventory-Service)

## 🙏 Teşekkürler

- Spring Boot ekibine harika framework için
- Docker ekibine konteynerizasyon teknolojisi için
- Tüm açık kaynak katkıda bulunanlara

---

⭐️ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!
