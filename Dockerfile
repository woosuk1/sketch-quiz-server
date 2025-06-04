# 1. Java 17 JDK 경량 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 임시 파일용 볼륨 생성
VOLUME /tmp

# 3. build/libs 폴더에 있는 JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 4. MongoDB URI를 환경변수로 받아 실행
ENTRYPOINT ["sh", "-c", "java -Dspring.data.mongodb.uri=$MONGODB_URI -jar /app.jar"]

##
