FROM gradle:4.8.1-jdk8 as build
COPY --chown=gradle:gradle . /tourguide
WORKDIR /tourguide
RUN gradle build

FROM openjdk:8-jre-slim
EXPOSE 9090
WORKDIR /app
WORKDIR test
COPY --from=build /tourguide/build/libs/*.jar /app/tourguide.jar
COPY --from=build /tourguide/build/reports/tests/test /app/test
ENTRYPOINT ["java","-jar","/app/tourguide.jar"]