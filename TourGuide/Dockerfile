FROM openjdk:8-jre-slim
EXPOSE 9090
ARG JAR_FILE=build/libs/*.jar
WORKDIR /tourguide
COPY ${JAR_FILE} tourguide.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /tourguide/tourguide.jar" ]