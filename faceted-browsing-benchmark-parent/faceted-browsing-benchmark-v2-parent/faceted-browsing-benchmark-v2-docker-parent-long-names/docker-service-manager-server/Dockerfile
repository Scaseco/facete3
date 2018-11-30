FROM openjdk:8-jdk-alpine
MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

RUN echo "Foobar1"

ARG JAR_FILE
ARG MAIN_CLASS
ARG ARTIFACT_ID
ARG MAIN_IMAGE

# Build args cannot be used in entrypoint
ENV JAR_FILE=${JAR_FILE}
ENV MAIN_CLASS=${MAIN_CLASS}
ENV ARTIFACT_ID=${ARTIFACT_ID}
ENV MAIN_IMAGE=${MAIN_IMAGE}

# Install the docker client
# https://stackoverflow.com/questions/38675925/is-it-possible-to-install-only-the-docker-cli-and-not-the-daemon
ARG DOCKER_VERSION=17.09.1-ce
RUN wget "https://download.docker.com/linux/static/stable/x86_64/docker-${DOCKER_VERSION}.tgz" \
  && tar xzvf "docker-${DOCKER_VERSION}.tgz" \
  && mv docker/docker /usr/local/bin \
  && rm -r docker "docker-${DOCKER_VERSION}.tgz"

RUN echo "Build args: ${JAR_FILE} ${MAIN_CLASS} ${ARTIFACT_ID} ${MAIN_IMAGE}"

WORKDIR /usr/share/${ARTIFACT_ID}/

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib lib
# Add the service itself
ADD target/${JAR_FILE} ${JAR_FILE}


ENTRYPOINT "/usr/bin/java" "-cp" ".:lib/*" "-Dloader.main=${MAIN_CLASS}" "org.springframework.boot.loader.PropertiesLauncher" "${MAIN_IMAGE}"

