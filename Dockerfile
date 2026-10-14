# Spring Boot backend: build with the Maven wrapper, run on a JRE as a non-root user.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# dependencies first, so they stay cached while only the sources change
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
# tests run in CI (they need Docker for Testcontainers), not inside the image build
RUN ./mvnw -B -q package -Dmaven.test.skip=true && cp target/chat-*.jar app.jar


FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S chat && adduser -S chat -G chat
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
USER chat
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
