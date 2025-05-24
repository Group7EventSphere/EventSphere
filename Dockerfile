# ─── Stage 1: Build the fat JAR ────────────────────────────────────────
FROM openjdk:21-jdk-slim AS builder
WORKDIR /workspace

# Option A: cache dependencies by globs
COPY gradlew .
COPY gradle gradle
COPY *.gradle* ./

RUN chmod +x gradlew \
 && ./gradlew dependencies --no-daemon

# then copy the rest of your source
COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon

# ─── Stage 2: Runtime ─────────────────────────────────────────────────
FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
