# --- Build Aşaması ---
# Maven ve Java 17 içeren bir temel imaj kullan (pom.xml'deki Java sürümüne göre ayarla)
FROM maven:3.9-eclipse-temurin-17 AS builder

# Çalışma dizinini ayarla
WORKDIR /build

# Önce pom.xml'i kopyala ve bağımlılıkları indir (Docker katman önbelleğinden faydalanmak için)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sonra kaynak kodunu kopyala
COPY src ./src

# Uygulamayı paketle (Spring Boot Maven plugin çalıştırılabilir JAR oluşturur)
# Not: pom.xml içindeki artifactId ve version'a göre JAR adını kontrol et!
RUN mvn clean package -DskipTests


# --- Run Aşaması ---
# Sadece Java Runtime içeren daha küçük bir temel imaj kullan
FROM eclipse-temurin:17-jre-alpine

# Çalışma dizinini ayarla
WORKDIR /app

# Build aşamasında oluşturulan JAR'ı kopyala
# Not: JAR adının doğru olduğundan emin ol! (artifactId-version.jar)
COPY --from=builder /build/target/kafka-consumer-db-0.0.1-SNAPSHOT.jar ./app.jar

# Bu uygulamanın dışarıya açması gereken bir portu yok (sadece Kafka ve DB'ye bağlanacak)
# EXPOSE 8083 # Gerekirse ileride eklenebilir

# Konteyner başladığında çalıştırılacak komut
ENTRYPOINT ["java", "-jar", "app.jar"]