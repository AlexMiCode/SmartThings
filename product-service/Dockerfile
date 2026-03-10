FROM gradle:8.10.2-jdk23 AS build
WORKDIR /workspace
COPY . .
RUN gradle :product-service:bootJar --no-daemon

FROM eclipse-temurin:23-jre
WORKDIR /app
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/product-service/build/libs/product-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java","-jar","/app/app.jar"]
