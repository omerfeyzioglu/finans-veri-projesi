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

# Uygulamayı paketle (Spring Boot'un layered jar özelliğini kullanabiliriz)
# pom.xml içinde <layers><enabled>true</enabled></layers> ayarı varsa bu daha verimli olur.
RUN mvn clean package -DskipTests


# --- Run Aşaması ---
# Sadece Java Runtime içeren daha küçük bir temel imaj kullan
FROM eclipse-temurin:17-jre-alpine

# Çalışma dizinini ayarla
WORKDIR /app

# Uygulama JAR'ını build aşamasından kopyala
# Not: JAR adının pom.xml'e göre doğru olduğundan emin ol (genellikle artifactId-version.jar olur)
COPY --from=builder /build/target/spring-platform-2-0.0.1-SNAPSHOT.jar ./app.jar

# Uygulamanın dinleyeceği portu dışarı aç (application.properties'deki port ile aynı)
EXPOSE 8080

# Konteyner başladığında çalıştırılacak komut
ENTRYPOINT ["java", "-jar", "app.jar"]

# Alternatif: Spring Boot'un Layered JAR'ını kullanıyorsanız (daha verimli):
# FROM maven:3.9-eclipse-temurin-17 AS builder
# WORKDIR /build
# COPY pom.xml .
# RUN mvn dependency:go-offline -B
# COPY src ./src
# RUN mvn package -DskipTests org.springframework.boot:spring-boot-maven-plugin:unpack

# FROM eclipse-temurin:17-jre-alpine
# WORKDIR /app
# COPY --from=builder /build/target/dependency/BOOT-INF/lib /app/lib
# COPY --from=builder /build/target/dependency/META-INF /app/META-INF
# COPY --from=builder /build/target/dependency/BOOT-INF/classes /app/classes
# ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]