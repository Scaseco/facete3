FROM java

ADD target/faceted-benchmark-1.0-SNAPSHOT.jar /faceted/faceted-benchmark.jar

WORKDIR /faceted

CMD java -cp faceted-benchmark.jar org.hobbit.core.run.ComponentStarter org.hobbit.benchmark.FacetedBenchmarkController