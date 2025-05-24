# ─── Stage 1: Build the JAR ─────────────────────────────────────────────
FROM openjdk:21-jdk-slim AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew \
 && ./gradlew dependencies --no-daemon

COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon

# ─── Stage 2: Package the app ───────────────────────────────────────────
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
