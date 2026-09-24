# Этап 1: Сборка JAR
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Копируем Maven wrapper и pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Скачиваем зависимости (кэширование слоев)
RUN ./mvnw dependency:go-offline -B

# Копируем исходники и собираем проект без прогона тестов (чтобы не требовать ключей на этапе сборки)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Этап 2: Финальный образ для запуска
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создаем папку для локальной БД
RUN mkdir -p /app/data

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx400m", "-Xms256m", "-jar", "app.jar"]