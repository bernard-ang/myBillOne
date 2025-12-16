# https://www.juliaaano.com/blog/2018/02/25/fast-java-builds-with-docker/

# --- stage 1: builder image
FROM maven:3.8.4-openjdk-11-slim as builder

COPY pom.xml /build/
COPY grabbill-core/pom.xml /build/grabbill-core/
COPY grabbill-engine/pom.xml /build/grabbill-engine/
COPY grabbill-server/pom.xml /build/grabbill-server/

RUN mvn -Dmaven.artifact.threads=20 --file build/pom.xml --batch-mode dependency:go-offline

COPY grabbill-core/src /build/grabbill-core/src/
COPY grabbill-engine/src /build/grabbill-engine/src/
COPY grabbill-server/src /build/grabbill-server/src/

# cannot use --offline because dependency:go-offline does not resolve all required dependencies
RUN mvn --file build/pom.xml --batch-mode package \
    && mkdir app \
    && mv /build/grabbill-engine/target/grabbill-engine-0.0.1-SNAPSHOT.jar app/engine.jar \
    && rm -fr build

# --- stage 2: production image
FROM openjdk:11-jre-slim

COPY --from=builder app/engine.jar app/engine.jar

CMD ["java", "-jar", "app/engine.jar"]

EXPOSE 8080
