FROM gradle:7.3-jdk17 AS build

WORKDIR /app

COPY settings.gradle.kts backend/build.gradle.kts ./
COPY gradle ./gradle

COPY backend/src ./src

COPY gradlew ./
COPY gradle ./gradle

RUN ./gradlew dependencies

RUN ./gradlew build

COPY backend/build/libs/backend-1.0-SNAPSHOT.jar /app/backend-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/app/backend-1.0-SNAPSHOT.jar"]