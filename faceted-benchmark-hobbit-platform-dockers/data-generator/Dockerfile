FROM imeq/java8-npm-bower-docker-maven
MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

ENV DEBIAN_FRONTEND noninteractive


RUN apt-get -y update --fix-missing && apt-get install -y libcairo2-dev libjpeg-dev libgif-dev
ADD target/faceted-benchmark-data-generator-1.0.0-SNAPSHOT.jar /faceted/faceteddata.jar

RUN git clone https://github.com/PoDiGG/podigg-lc.git /faceted/podigg
WORKDIR /faceted/podigg
RUN npm install

WORKDIR /faceted

CMD java -cp faceteddata.jar org.hobbit.core.run.ComponentStarter org.hobbit.SampleDataGenerator
