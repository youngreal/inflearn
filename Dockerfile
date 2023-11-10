FROM openjdk:17
ARG JAR_FILE=build/libs/musinsa-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} ./musinsa-0.0.1-SNAPSHOT.jar
ENV TZ=Asia/Seoul
#컨테이너 실행될떄 수행될 명령어 지정
ENTRYPOINT ["java", "-jar", "./musinsa-0.0.1-SNAPSHOT.jar"]
