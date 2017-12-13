FROM openjdk:8-jdk-alpine
MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

ARG ARTIFACT_ID
ARG JAR_FILE
ARG MAIN_CLASS
ARG MAIN_IMAGE

WORKDIR /usr/share/${ARTIFACT_ID}/

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib lib
# Add the service itself
ADD target/${JAR_FILE} ${JAR_FILE}


ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/${ARTIFACT_ID}/${JAR_FILE}", MAIN_CLASS, MAIN_IMAGE]
