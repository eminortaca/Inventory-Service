# Servisler Birbiriyle Nasıl İletişim Kuruyor?

Bu projede **Order Service** ve **Inventory Service** arasındaki iletişim mekanizmasını açıklayacağız.

---

## 1. Mimari Genel Bakış

```
Order Service (Port: 8081)
    ↓ (HTTP REST Call)
Inventory Service (Port: 8080)
    ↓ (Database Query)
PostgreSQL Database
```

---

## 2. Order Service'ten Inventory Service'e İletişim

### A. WebClient Configuration (`WebClientConfig.java`)

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

**Açıklama:**
- `WebClient.Builder` bir Spring Bean olarak tanımlanıyor
- Bu Bean, tüm HTTP istekleri yapmanız için reküel olarak kullanılır
- Non-blocking ve asynchronous HTTP istekleri gönderir

---

## 3. OrderService'de Inventory Service'i Çağırma

### B. OrderService.java - Inventory Service Çağrısı

```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        // ... Sipariş oluşturma işlemleri ...
        
        // Inventory Service'e GET isteği gönderiliyor
        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri("http://inventory-service:8080/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block(); // Cevap gelene kadar bekler

        // Stok kontrolü yapılıyor...
    }
}
```

**Adım Adım Açıklama:**

1. **WebClient Oluşturma**: `webClientBuilder.build()` - WebClient instance'ı oluşturur

2. **HTTP GET İsteği**: `.get()` - GET metodu seçilir

3. **URI Ayarlama**: `.uri("http://inventory-service:8080/api/inventory", ...)` 
   - `inventory-service` = Docker network'teki service adı
   - `8080` = Inventory Service'in çalıştığı port
   - `/api/inventory` = Controller'daki endpoint

4. **Query Parameters**: `.queryParam("skuCode", skuCodes)`
   - URL: `http://inventory-service:8080/api/inventory?skuCode=iphone_13&skuCode=iphone_13_red`

5. **Response Alma**: `.retrieve()` - HTTP yanıtını alır

6. **Response Dönüştürme**: `.bodyToMono(InventoryResponse[].class)`
   - JSON yanıtı Java objesine dönüştürür
   - `Mono` = Reactive type (0 veya 1 öğe)

7. **Blocking Çağrı**: `.block()`
   - Asynchronous çağrıyı synchronous hale getirir
   - Cevap gelene kadar bu satırda bekler

---

## 4. Inventory Service'in Sunduğu Endpoint

### C. InventoryController.java

```java
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}
```

**Fonksiyonalite:**
- **Endpoint**: `GET /api/inventory`
- **Query Parametresi**: `skuCode` (birden fazla değer alabilir)
- **Dönüş**: `List<InventoryResponse>` - Stok bilgilerini içeren liste

---

## 5. Docker Network Yapılandırması

### D. docker-compose.yml

```yaml
services:
  inventory-db:
    image: postgres:15
    environment:
      - POSTGRES_DB=inventorydb
    networks:
      - microservices-network

  inventory-service:
    build: ./inventory-service
    ports:
      - "8081:8080"
    networks:
      - microservices-network

  order-service:
    build: ./order-service
    ports:
      - "8082:8081"
    networks:
      - microservices-network

networks:
  microservices-network:
    driver: bridge
```

**Ağ Yapısı:**
- Tüm servisler `microservices-network` adlı bridge network'e bağlı
- Docker otomatik olarak service adlarını internal IP'lere çevirir
- Order Service, `inventory-service` adını kullanarak doğrudan Inventory Service'e erişir

---

## 6. İletişim Akışı Diyagramı

```
1. Client
   ↓
2. Order Service: POST /api/orders
   ↓
3. OrderService.placeOrder() çağrılır
   ↓
4. WebClient GET isteği: http://inventory-service:8080/api/inventory?skuCode=...
   ↓
5. Inventory Service: @GetMapping /api/inventory
   ↓
6. InventoryService.isInStock() sorgulanır
   ↓
7. PostgreSQL: SELECT * FROM inventory WHERE sku_code IN (...)
   ↓
8. InventoryResponse[] döndürülür
   ↓
9. Order Service: Stok kontrolü yapılır
   ↓
10. OrderRepository'ye sipariş kaydedilir
   ↓
11. Yanıt Client'e gönderilir
```

---

## 7. Veri Modelleri

### InventoryResponse (Her iki serviste de bulunur)

```java
@Data
@AllArgsConstructor
public class InventoryResponse {
    private String skuCode;
    private boolean isInStock;
}
```

---

## 8. Hata Çözümleme

### Problem: "inventory-service: Name or service not known"

**Çözüm:**
- `docker-compose.yml`'de `microservices-network` tanımlanmalı
- Tüm servisler aynı network'e bağlı olmalı
- Service adları (inventory-service, order-service) doğru yazılmalı

### Problem: Connection Timeout

**Çözüm:**
- Inventory Service çalışıyor mu kontrol et: `docker ps`
- Port numaraları doğru mu: 8080 (internal), 8081:8080 (mapping)
- PostgreSQL başladı mı: `docker-compose logs inventory-db`

---

## 9. Teknolojiler

| Bileşen | Teknoloji |
|---------|-----------|
| HTTP İletişim | WebClient (Spring WebFlux) |
| Messaging | RESTful API |
| Network | Docker Compose Bridge Network |
| Database | PostgreSQL |
| Service Discovery | Docker DNS |

---

## 10. Gelecek İyileştirmeler

1. **Circuit Breaker** (Resilience4J): Inventory Service başarısız olursa fallback
2. **Retry Logic**: Geçici hatalar için otomatik tekrar
3. **Service Registry** (Eureka): Dinamik service discovery
4. **API Gateway** (Zuul/Spring Cloud Gateway): Merkezi giriş noktası
5. **Async Messaging** (RabbitMQ/Kafka): Event-driven architecture


