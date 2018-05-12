FROM java

ADD target/task-generator-1.0-SNAPSHOT.jar /faceted/facetedtask.jar

WORKDIR /faceted

CMD java -cp facetedtask.jar org.hobbit.core.run.ComponentStarter org.hobbit.task.FacetedTaskGenerator