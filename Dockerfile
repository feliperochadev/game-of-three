FROM gradle:6.8.1-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon

FROM azul/zulu-openjdk-alpine:11.0.7-jre
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xmx1536m -Xms128m"
RUN echo "${JAVA_OPTS}"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]