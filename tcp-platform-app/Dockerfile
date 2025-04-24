# --- Build Aşaması ---
# Maven ve Java 17 içeren bir temel imaj kullan (pom.xml'deki Java sürümüne göre ayarla)
# Eğer Java 22 kullanıyorsan: FROM maven:3.9-eclipse-temurin-22 AS builder
FROM maven:3.9-eclipse-temurin-17 AS builder

# Uygulama kodunun kopyalanacağı çalışma dizinini ayarla
WORKDIR /app

# Önce pom.xml'i kopyala (bağımlılıkları indirmek için)
COPY pom.xml .

# Bağımlılıkları indir (opsiyonel, build süresini hızlandırabilir)
# RUN mvn dependency:go-offline -B

# Tüm kaynak kodunu kopyala
COPY src ./src

# Maven ile uygulamayı paketle (fat JAR oluştur ve testleri atla)
# pom.xml'deki assembly plugin'in fat JAR ürettiğinden emin ol
# Not: Eğer assembly plugin JAR ismini farklı yapıyorsa aşağıdaki COPY komutunu güncellemelisin.
RUN mvn clean package -DskipTests


# --- Run Aşaması ---
# Sadece Java Runtime içeren daha küçük bir temel imaj kullan
# Eğer Java 22 kullanıyorsan: FROM eclipse-temurin:22-jre-alpine
FROM eclipse-temurin:17-jre-alpine

# Çalışma dizinini ayarla
WORKDIR /app

# Build aşamasında oluşturulan fat JAR'ı kopyala ve adını basitleştir
# Not: JAR dosyasının adının pom.xml'deki build ayarlarına uygun olduğundan emin ol!
COPY --from=builder /app/target/tcp-platform-app-1.0-SNAPSHOT-jar-with-dependencies.jar ./app.jar

# Uygulamanın dinleyeceği portu dışarı aç (rates-config.json'daki port ile aynı olmalı)
EXPOSE 8081

# Konteyner başladığında çalıştırılacak komut
ENTRYPOINT ["java", "-jar", "app.jar"]