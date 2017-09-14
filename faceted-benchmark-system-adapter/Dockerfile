FROM java

ADD target/system-adapter-1.0-SNAPSHOT.jar /faceted/facetedsystem.jar
ADD target/classes/lib/virt_jena3.jar /faceted/lib/virt_jena3.jar
ADD target/classes/lib/virtjdbc4.jar /faceted/lib/virtjdbc4.jar

WORKDIR /faceted

CMD java -cp facetedsystem.jar org.hobbit.core.run.ComponentStarter org.hobbit.system.FacetedSystemAdapterr
