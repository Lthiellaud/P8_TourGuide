FROM openjdk:8
EXPOSE 9090
ARG JAR_FILE=build/libs/*.jar
WORKDIR /tourGuide
COPY ${JAR_FILE} tourGuide.jar
ENTRYPOINT ["java","-jar","./tourGuide.jar"]