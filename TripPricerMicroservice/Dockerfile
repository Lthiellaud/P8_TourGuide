FROM openjdk:8-jre-slim
EXPOSE 9093
ARG JAR_FILE=build/libs/*.jar
WORKDIR /TripPricer
COPY ${JAR_FILE} trippricer.jar
ENTRYPOINT ["java","-jar","./trippricer.jar"]