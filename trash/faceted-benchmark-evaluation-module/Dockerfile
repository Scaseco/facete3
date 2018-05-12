FROM java

ADD target/evaluation-module-1.0-SNAPSHOT.jar /faceted/facetedevaluationmodule.jar

WORKDIR /faceted

CMD java -cp facetedevaluationmodule.jar org.hobbit.core.run.ComponentStarter org.hobbit.evaluation.EvaluationModule