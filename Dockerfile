# ----------------------------------------------------------------------------------------------------------------------
# Compiling image
# ----------------------------------------------------------------------------------------------------------------------
FROM ubuntu:20.04 as compiler

MAINTAINER borisskert <boris.skert@gmail.com>
ENV DEBIAN_FRONTEND	noninteractive

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk

COPY . /usr/local/src
WORKDIR /usr/local/src

RUN ./mvnw package


# ----------------------------------------------------------------------------------------------------------------------
# Runtime image
# ----------------------------------------------------------------------------------------------------------------------
FROM ubuntu:20.04

RUN apt-get update && \
    apt-get install -y openjdk-11-jre

COPY --from=compiler /usr/local/src/target/spring-jwt.jar /usr/local/lib/spring-jwt.jar

ENTRYPOINT ["java", "-jar", "/usr/local/lib/spring-jwt.jar"]
