# Faceted Browsing Benchmark

The benchmark on Faceted Browsing aims to benchmark systems on their performance to support
browsing through linked data by iterative transitions performed by an intelligent user. By developing
realistic browsing scenarios through a dataset that comprises of different structural challenges for the
system, we aim to test its performance with respect to several choke points in a real world scenario.


# Uploading a System to the HOBBIT platform

Guidelines on how to upload a benchmark can be found here: https://github.com/hobbit-project/platform/wiki/Benchmark-your-system


# Running the benchmark

If you want to run the benchmark using the platform, please follow the guidelines found here: https://github.com/hobbit-project/platform/wiki/Experiments


# Description of the Faceted Browsing parameters

All you need to provide is an integer value for a seed which allows for the randomization of the benchmark scenarios. 




## Changelog

* Created `hobbit-sdk-core` artifact which defines abstract default components according to the platform specification (bc, dg, tg, sa, es, em).
* Created `hobbit-sdk-rdf` module for RDF/SPARQL-centric benchmarks
  * Provides e.g. jena-virtuoso adapter, further vendor drivers could be added
* Docker container creation refactored:
  * There is now a `DockerServiceFactory` interface with three fundamental implementations:
    * `DockerServiceFactoryDockerClient` creates real docker containers using Spotify's docker client library
    * `DockerServiceFactoryDelegating` implements the DockerServiceFactory interface, but delegates to custom (lambda) functions; mostly for use to intantiate those component classes that would run in separate, real docker containers. 
    * `DockerServiceManagerClientComponent` a `DockerServiceFactory` that translates container creation requests to messages on a (command) channel
  * Furthermore, the class which can handle docker container creation requests on a channel is
    * `DockerServiceMangerServerComponent` a component that listens on a command channel and delegates container creation requests to calls to a configured DockerServiceFactory
* All component communication refactored
  * using rxJava flows; components are not dependent on RabbitMQ anymore
  * Implemented flow wrappers for RabbitMQ
 
 

### Pitfalls for developers

Expression transformation (issue last encountered: jena 3.8.0)
```
// NEVER directly apply node transform when variables may be involved; they get wrongly wrapped
// and warnings will be logged
// The resulting expression will not evaluate correctly despite its syntactic (string) representation being correct
never = expr.applyNodeTransform(FacetConstraintImpl::blankNodeToVar);

CORRECT This handles variables correctly
correct = ExprTransformer.transform(new NodeTransformExpr(FacetConstraintImpl::blankNodeToVar), expr);

```


