# ─── Stage 1: Build the JAR ─────────────────────────────────────────────
FROM openjdk:21-jdk-slim AS builder

# set working dir
WORKDIR /workspace

# copy Gradle wrapper and settings
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# make the wrapper executable & download dependencies
RUN chmod +x gradlew \
 && ./gradlew dependencies --no-daemon

# copy the rest of the source and build
COPY . .
RUN ./gradlew clean bootJar -x test --no-daemon


# ─── Stage 2: Package the app ───────────────────────────────────────────
FROM openjdk:21-jre-slim

WORKDIR /app

# copy the fat JAR from the builder stage
COPY --from=builder /workspace/build/libs/*.jar app.jar

# expose the internal Spring Boot port
EXPOSE 8080

# run the app
ENTRYPOINT ["java","-jar","app.jar"]
