FROM gradle:9.4.1-jdk21 AS build

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts build.gradle.kts ./
COPY src src

RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21.0.10_7-jre

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/db /app/logs

EXPOSE 18080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
