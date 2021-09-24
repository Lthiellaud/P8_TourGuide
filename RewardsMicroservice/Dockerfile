FROM openjdk:8-jre-slim
EXPOSE 9092
ARG JAR_FILE=build/libs/*.jar
WORKDIR /Rewards
COPY ${JAR_FILE} rewards.jar
ENTRYPOINT ["java","-jar","./rewards.jar"]