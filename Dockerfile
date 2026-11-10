# Build stage: the hosted build downloads dependencies and packages the application.
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime stage: browser users need no local runtime or development tools.
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /app/data && chown -R 10001:10001 /app
COPY --from=build /workspace/target/cloudguard-0.1.0.jar /app/cloudguard.jar
USER 10001:10001
ENV PORT=8080
EXPOSE 8080
VOLUME ["/app/data"]
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70.0","-jar","/app/cloudguard.jar"]
