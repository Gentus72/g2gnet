FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/*.jar /app

EXPOSE 7000

COPY ccserver/ /app/ccserver

COPY server/ /app/server

COPY client/res/ /app/client/res

# ENTRYPOINT ["java", "-jar", "app.jar"]
