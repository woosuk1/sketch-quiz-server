FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY app.jar /app.jar

ENTRYPOINT ["java", "-Dspring.data.mongodb.uri=${MONGODB_URI}", "-Dspring.redis.host=${REDIS_HOST}", "-Dspring.redis.port=${REDIS_PORT}", "-jar", "/app.jar"]
