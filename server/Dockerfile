FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/server-0.0.1.jar app.jar
COPY src/main/resources/rules rules
ENTRYPOINT ["java","-jar","/app.jar","--localDbRules=/rules"]
