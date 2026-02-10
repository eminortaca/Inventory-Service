# Docker Build Hatası Çözümleme Rehberi

## 🔴 Aldığınız Hata

```
[ERROR] No goals have been specified for this build. You must specify a valid lifecycle phase or a goal
```

---

## ❌ Sorunun Nedeni

Bu hata Docker build sırasında oluştu, çünkü:

1. **Order Service'in pom.xml'de eski dependency:** `spring-boot-starter-webmvc` kullanılıyordu
   - Bu artifact Spring Boot 4.0.2'de mevcut değil
   - Spring 7.0 ile uyumlu değil

2. **Inventory Service'in pom.xml'de eksik kütüphaneler:**
   - PostgreSQL JDBC driver yüklü değildi
   - Hibernate PostgreSQL dialect bulunamıyordu

3. **Docker'da Maven özel goal belirtilmemiş:**
   - Dockerfile'da `mvn clean package` çalışırken bağımlılıklar başarısız oldu

---

## ✅ Yapılan Çözümler

### 1. Order Service pom.xml Düzeltme

**Eski (Yanlış):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

**Yeni (Doğru):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Neden?**
- `spring-boot-starter-web` = Web uygulamaları için (Spring MVC + Tomcat)
- `spring-boot-starter-webmvc` = Spring 4.0.2'de yok, sadece eski versiyonlarda vardı

### 2. Inventory Service pom.xml Düzeltme

**Eklenen:**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Neden?**
- PostgreSQL veritabanı driver'ını sağlar
- Docker-compose'da PostgreSQL kullanıyoruz
- JDBC bağlantısı kurmak için gerekli

### 3. Order Service application.properties Iyileştirmesi

**Eklenen:**
```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**Neden?**
- Order Service H2 in-memory veritabanı kullanıyor
- Test ve geliştirme için yeterli
- H2 Console'a erişim sağlıyor

### 4. Docker-compose PostgreSQL Konfigürasyonu

**Eklenen:**
```yaml
command:
  - "sh"
  - "-c"
  - "createdb -U postgres inventorydb 2>/dev/null || true && postgres"
```

**Neden?**
- PostgreSQL konteyner başlarken otomatik database oluşturması sağlıyor
- 'inventorydb' veritabanını başlangıçta yaratıyor
- Hata varsa görmezden gelip yine de çalışmaya devam ediyor

---

## 🧪 Çözümün Test Edilmesi

### Adım 1: Eski Containers'ı Temizle

```bash
# Docker-compose'u durdur
docker-compose down -v

# Kalan containers'ları sil
docker system prune -a

# Volumes'leri sil
docker volume prune
```

### Adım 2: Yeniden Build Et

```bash
cd C:\Users\ASUS\Desktop\proje
docker-compose up --build
```

### Adım 3: Durumu Kontrol Et

```bash
# Başka terminal'de
docker-compose ps

# Çıktı şöyle olmalı:
# proje-inventory-db-1         Up 
# proje-inventory-service-1    Up
# proje-order-service-1        Up
```

### Adım 4: Servisleri Test Et

```bash
# Inventory Service çalışıyor mu?
curl http://localhost:8081/api/inventory?skuCode=test

# Order Service çalışıyor mu?
curl http://localhost:8082/api/orders

# Günlükleri kontrol et
docker-compose logs inventory-service
docker-compose logs order-service
```

---

## 📊 Sorun ve Çözüm Tablosu

| Sorun | Neden | Çözüm |
|-------|-------|-------|
| ClassNotFoundException: WebMvcAutoConfiguration | spring-boot-starter-webmvc eski | spring-boot-starter-web kullan |
| FATAL: database "inventorydb" does not exist | PostgreSQL container'ı database oluşturmadı | docker-compose command ekle |
| Unable to open JDBC Connection | PostgreSQL driver yok | postgresql dependency ekle |
| Connection refused | Service başlanmadı | docker logs kontrol et |
| Port already in use | Eski container çalışıyor | docker-compose down -v yap |

---

## 💡 En İyi Uygulamalar

### 1. Bağımlılıkları İyi Seçin

```xml
<!-- ❌ YANLIŞ -->
<artifactId>spring-boot-starter-webmvc</artifactId>

<!-- ✅ DOĞRU -->
<artifactId>spring-boot-starter-web</artifactId>
```

### 2. Veritabanı Driver'ını Ekleyin

```xml
<!-- PostgreSQL için -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- MySQL için -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 3. Docker Network'ü Doğru Yapılandırın

```yaml
networks:
  microservices-network:
    driver: bridge

services:
  service1:
    networks:
      - microservices-network
  service2:
    networks:
      - microservices-network
```

### 4. Health Check Ekleyin

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

---

## 🔧 Debug İçin Yararlı Komutlar

```bash
# Container'ın bash'ine gir
docker-compose exec inventory-service bash

# Logs'u real-time gör
docker-compose logs -f inventory-service

# Container'ın çevresel değişkenlerini gör
docker-compose exec inventory-service env

# Network'ü kontrol et
docker network ls
docker network inspect proje_microservices-network

# Portları kontrol et
netstat -an | grep LISTENING

# Docker image'ları listele
docker images | grep proje

# Build cache'i temizle
docker system prune --all --force
```

---

## 🚨 Yaygın Hatalar ve Çözümleri

### Hata 1: Maven Resolution Sorunu
```
[ERROR] Could not find artifact ...
```
**Çözüm:** POM.xml dependency'leri kontrol edin ve Maven cache'ini temizleyin
```bash
mvn clean
rm -rf ~/.m2/repository
```

### Hata 2: Connection Timeout
```
org.postgresql.util.PSQLException: Connection timeout
```
**Çözüm:** PostgreSQL container'ın başlamış olduğundan emin olun
```bash
docker-compose logs inventory-db
```

### Hata 3: Port Binding Hatası
```
Address already in use
```
**Çözüm:** Eski container'ları temizleyin
```bash
docker-compose down
docker container prune
```

---

## 📈 Geliştirilmiş Yapı

Yukarıdaki düzeltmelerin ardından:

✅ Order Service başarıyla derlenecek
✅ Inventory Service PostgreSQL'e bağlanacak
✅ WebClient iletişimi çalışacak
✅ Docker containers sorunsuz çalışacak
✅ Servisler birbirleriyle iletişim kurabilecek

---

## 🎯 Sonuç

Tüm sorunlar aşağıdakiler düzeltilerek çözüldü:
1. ✅ Yanlış Spring Boot starter dependency'si değiştirildi
2. ✅ PostgreSQL driver dependency'si eklendi
3. ✅ Database konfigürasyonları tamamlandı
4. ✅ Docker-compose script'i iyileştirildi
5. ✅ Network konfigürasyonu doğrulandı

**Şimdi proje çalışır durumda olmalıdır!**


