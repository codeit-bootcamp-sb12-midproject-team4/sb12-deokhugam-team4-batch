FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/*-SNAPSHOT.jar app.jar

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-jar", "app.jar"]