# syntax=docker/dockerfile:1
FROM maven:3.10.0-eclipse-temurin-25-noble AS build
WORKDIR /build
ARG APP_DIR=.
COPY ${APP_DIR}/pom.xml ./pom.xml
COPY ${APP_DIR}/src ./src
RUN --mount=type=cache,id=ksynerx-maven,target=/root/.m2,sharing=locked \
    mvn -B -ntp package -DskipTests && cp target/*.jar /build/app.jar

FROM eclipse-temurin:25-jre-noble AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/app.jar ./app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
