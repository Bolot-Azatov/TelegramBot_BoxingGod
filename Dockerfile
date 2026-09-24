# 1. Этап сборки
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Копируем wrapper и pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Копируем исходный код
COPY src ./src

# Собираем JAR без тестов
RUN ./mvnw clean package -DskipTests -B

# 2. Этап запуска
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/data
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx400m", "-Xms256m", "-jar", "app.jar"]