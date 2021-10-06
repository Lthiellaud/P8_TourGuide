FROM openjdk:8-jre-slim
EXPOSE 9091
ARG JAR_FILE=build/libs/*.jar
WORKDIR /Gps
COPY ${JAR_FILE} gps.jar
ENTRYPOINT ["java","-jar","./gps.jar"]