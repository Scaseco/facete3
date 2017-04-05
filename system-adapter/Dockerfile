FROM java

ADD target/system-adapter-1.0-SNAPSHOT.jar /faceted/facetedsystem.jar

WORKDIR /faceted

CMD java -cp facetedsystem.jar org.hobbit.core.run.ComponentStarter org.hobbit.system.FacetedSystemAdapter