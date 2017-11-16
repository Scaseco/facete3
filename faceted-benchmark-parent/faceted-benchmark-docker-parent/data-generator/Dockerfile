FROM openjdk:8-jdk-alpine
MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

WORKDIR /usr/share/myservice/

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} myservice.jar


ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/myservice/myservice.jar"]
