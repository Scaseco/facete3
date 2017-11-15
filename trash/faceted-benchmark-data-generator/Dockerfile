FROM java

ADD target/faceted-benchmark-data-generator-1.0.0-SNAPSHOT.jar /faceted/faceteddata.jar

WORKDIR /faceted

CMD java -cp faceteddata.jar org.hobbit.core.run.ComponentStarter org.hobbit.SampleDataGenerator
