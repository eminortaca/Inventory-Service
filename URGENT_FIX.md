# 🚨 ACIL FİKS - POM.XML SORUNLARI

## Sorun Nedir?

Order Service ve Inventory Service'de yanlış test dependencies bulunuyor:

```xml
<!-- ❌ YANLIŞ - Spring Boot 4.0.2'de YOK -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Çözüm (Her iki pom.xml için de aynı)

### Order Service: `order-service/pom.xml`

**İBulunacak Bölüm (satır 53-63):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

**Bunu ile Değiştir:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### Inventory Service: `inventory-service/pom.xml`

**Aynı sorunu düzelt (satır 50-76 civarında)**

Yanlış bölüm:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

**Düzeltilmiş Hali:**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## ✅ Sonra Ne Yapmalısın?

1. **POM dosyalarını düzelt** (yukarıdaki gibi)

2. **Docker-compose'u sıfırla:**
```bash
cd C:\Users\ASUS\Desktop\proje
docker-compose down -v
docker system prune -a -f
```

3. **Yeniden inşa et:**
```bash
docker-compose up --build
```

4. **Kontrol et:**
```bash
docker-compose ps
```

---

## 🎯 Niye Bu Yanlış?

- `spring-boot-starter-data-jpa-test` → Spring Boot 4.0.2'de YOK
- `spring-boot-starter-webmvc-test` → Spring Boot 4.0.2'de YOK
- Bu dependencies Spring Boot'un eski versiyonlarında vardı

Sadece gerçek dependencies kullan:
- ✅ `spring-boot-starter-web`
- ✅ `spring-boot-starter-data-jpa`
- ✅ `spring-boot-starter-webflux`

Test yazacaksan Spring Boot Test'i dependencies'lerde kur, starter-test parametresi ile.


