FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -DskipTests dependency:go-offline

COPY src/ src/

FROM builder AS test
RUN ./mvnw test

FROM builder AS package
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=package /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]