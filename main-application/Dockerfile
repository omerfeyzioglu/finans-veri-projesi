# --- Build Aşaması ---
# Maven ve Java 17 içeren bir temel imaj kullan (pom.xml'deki Java sürümüne göre ayarla)
FROM maven:3.9-eclipse-temurin-17 AS builder

# Çalışma dizinini ayarla
WORKDIR /build

# Önce pom.xml'i kopyala ve bağımlılıkları indir
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sonra kaynak kodunu kopyala
COPY src ./src

# Uygulamayı paketle
# Not: pom.xml içindeki artifactId'ye göre JAR adını kontrol et!
RUN mvn clean package -DskipTests

# --- Run Aşaması ---
# Sadece Java Runtime içeren daha küçük bir temel imaj kullan
FROM eclipse-temurin:17-jre-alpine

# Çalışma dizinini ayarla
WORKDIR /app

# Build aşamasında oluşturulan JAR'ı kopyala
# Not: JAR adının doğru olduğundan emin ol! Genellikle target/artifactId-version.jar olur.
COPY --from=builder /build/target/main-application-0.0.1-SNAPSHOT.jar ./app.jar

# Ana uygulamanın dışarıya açması gereken bir portu VARSAYILAN olarak yok,
# ama uygulamanın çalışması için bu yeterli. İleride bir port gerekirse buraya EXPOSE ekleriz.
# EXPOSE 8082 # Örneğin, ileride bir durum endpoint'i eklersek

# Konteyner başladığında çalıştırılacak komut
ENTRYPOINT ["java", "-jar", "app.jar"]