FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon

COPY src src
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:17-jdk-alpine as jre-build
COPY --from=build /app/build/libs/*.jar /app/app.jar
RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.logging,java.xml,java.sql,java.desktop,java.management,java.naming,java.security.sasl,java.instrument \
    --add-modules jdk.unsupported,jdk.crypto.ec \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /javaruntime


FROM alpine:3.15
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

WORKDIR /app
COPY --from=jre-build /app/app.jar /app/app.jar


CMD ["java", "-jar", "/app/app.jar"]

