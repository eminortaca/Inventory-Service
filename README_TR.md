# Microservices Projesi - Kurulum ve Çalıştırma Rehberi

## 📋 Proje Yapısı

Bu proje, Spring Boot kullanarak yazılmış iki adet microservice'ten oluşmaktadır:

1. **Inventory Service** (Port: 8080)
   - Ürün stok bilgilerini yönetir
   - PostgreSQL veritabanını kullanır
   - REST API sağlar

2. **Order Service** (Port: 8081)
   - Sipariş oluşturma ve yönetimi
   - Inventory Service ile iletişim kurar
   - H2 veritabanı (geliştirme amaçlı)

---

## 🔧 Ön Koşullar

- Docker ve Docker Compose yüklü olmalı
- Java 21+ (yerel çalıştırma için)
- Maven 3.9.6+ (yerel derleme için)
- Git

---

## 🚀 Hızlı Başlangıç

### 1. Docker ile Çalıştırma (Önerilen)

```bash
# Proje dizinine gidin
cd C:\Users\ASUS\Desktop\proje

# Docker containers'ını build ve başlatın
docker-compose up --build

# Kontrol etmek için (başka bir terminal)
docker-compose ps
```

**Expected Output:**
```
NAME                   STATUS              PORTS
proje-inventory-db-1   Up 2 seconds        5432:5432
proje-inventory-service-1   Up 1 second    8081:8080
proje-order-service-1  Up 1 second         8082:8081
```

### 2. Yerel Çalıştırma

#### Adım 1: PostgreSQL Başlatın
```bash
docker run -d \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=inventorydb \
  -p 5432:5432 \
  --name inventory-postgres \
  postgres:15
```

#### Adım 2: Inventory Service'i Derleyin ve Çalıştırın
```bash
cd inventory-service
mvn clean package
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar
```

#### Adım 3: Order Service'i Derleyin ve Çalıştırın (Başka terminal)
```bash
cd order-service
mvn clean package
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

---

## 📡 API Endpoints

### Inventory Service

#### 1. Stok Kontrol Etme
```bash
GET http://localhost:8081/api/inventory?skuCode=iphone_13&skuCode=iphone_13_red

Response:
[
    {
        "skuCode": "iphone_13",
        "isInStock": true
    },
    {
        "skuCode": "iphone_13_red",
        "isInStock": false
    }
]
```

### Order Service

#### 1. Sipariş Oluşturma
```bash
POST http://localhost:8082/api/orders
Content-Type: application/json

{
    "orderLineItemsDtoList": [
        {
            "skuCode": "iphone_13",
            "price": 1200,
            "quantity": 2
        }
    ]
}

Response:
{
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "price": 2400.0,
    "orderLineItemsList": [...]
}
```

---

## 🔌 Servisler Arası İletişim

### Order Service → Inventory Service

**Yapılandırma:** `WebClientConfig.java`
```java
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

**Kullanım:** `OrderService.java`
```java
InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
    .uri("http://inventory-service:8080/api/inventory",
        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
    .retrieve()
    .bodyToMono(InventoryResponse[].class)
    .block();
```

**Detaylı Açıklama:** `MICROSERVICES_COMMUNICATION.md` dosyasını okuyun.

---

## 🗄️ Veritabanı Yapılandırması

### Inventory Service (PostgreSQL)

**Bağlantı Bilgileri:**
- Host: `localhost` (docker-compose: `inventory-db`)
- Port: `5432`
- Database: `inventorydb`
- Username: `postgres`
- Password: `password`

**Environment Variables:**
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://inventory-db:5432/inventorydb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
```

### Order Service (H2)

**Veritabanı Türü:** H2 (In-Memory)
**Konsol Erişimi:** `http://localhost:8082/h2-console`

---

## 🐛 Hata Çözümü

### 1. "Connection refused" Hatası
```
Sorun: Inventory Service başlamadı
Çözüm: 
  1. docker-compose logs inventory-service
  2. PostgreSQL'nin çalışıp çalışmadığını kontrol edin
  3. Portların boş olup olmadığını kontrol edin
```

### 2. "Network not found" Hatası
```
Sorun: Docker network oluşturulmadı
Çözüm:
  docker network ls
  docker-compose down
  docker-compose up --build
```

### 3. "No database selected" Hatası
```
Sorun: PostgreSQL veritabanı oluşturulmadı
Çözüm:
  docker-compose logs inventory-db
  PostgreSQL container'ı silin ve yeniden başlatın
```

---

## 📊 Port Haritalaması

| Service | Internal Port | External Port |
|---------|---------------|---------------|
| Inventory Service | 8080 | 8081 |
| Order Service | 8081 | 8082 |
| PostgreSQL | 5432 | 5432 |
| H2 Console | - | 8082 |

---

## 📝 Docker Compose Komutları

```bash
# Containers'ı başlat
docker-compose up -d

# Logs'ları görüntüle
docker-compose logs -f inventory-service
docker-compose logs -f order-service

# Container'ları durdur
docker-compose down

# Volume'leri da sil
docker-compose down -v

# Specific service başlat
docker-compose up -d inventory-service

# Rebuild et
docker-compose up --build
```

---

## 🔍 Kontrol Listesi

- [ ] Docker ve Docker Compose yüklü
- [ ] Portlar 8081, 8082, 5432 boş
- [ ] `docker-compose up --build` başarılı oldu
- [ ] `docker-compose ps` tüm servisleri "Up" olarak gösteriyor
- [ ] Inventory Service'e HTTP GET isteği çalışıyor
- [ ] Order Service'e HTTP POST isteği çalışıyor
- [ ] Order Service başarıyla Inventory Service'i çağırıyor

---

## 📚 Ek Kaynaklar

- **Microservices İletişimi:** `MICROSERVICES_COMMUNICATION.md`
- **Spring Boot Belgeleri:** https://spring.io/projects/spring-boot
- **Docker Compose Belgeleri:** https://docs.docker.com/compose/
- **WebClient Belgeleri:** https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html

---

## 👨‍💼 Proje Bilgileri

- **Framework:** Spring Boot 4.0.2
- **Java Version:** 21
- **Build Tool:** Maven 3.9.6
- **Database:** PostgreSQL 15, H2
- **Containerization:** Docker & Docker Compose

---

## 💡 İpuçları

1. **Günlükleri Takip Edin:** `docker-compose logs -f` ile real-time günlükleri görün
2. **Sağlık Kontrolleri:** Service health endpoints ekleyebilirsiniz
3. **Graceful Shutdown:** Containers'ı `docker-compose down` ile kapayın
4. **Veri Persistance:** PostgreSQL volume'leri kullanıyor, veriler korunuyor

---

**Yazarı:** Microservices Projesi
**Tarih:** 2026-02-10
**Versiyon:** 1.0


