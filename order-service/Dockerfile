FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# SADECE pom.xml'i kopyala ve kütüphaneleri indir
COPY pom.xml .
RUN mvn dependency:go-offline

# ŞİMDİ kodları kopyala (Kod değişse bile üstteki katman sabit kalır)
COPY src ./src
RUN mvn clean package -DskipTests

# ... geri kalanı aynı