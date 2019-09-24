# Faceted Search Benchmark Generation Framework (FSBG)

This repository contains an advanced faceted search benchmark based on the Facete3 faceted search engine for SPARQL-accessible knowledge graphs.

## Motivation

Faceted search allows humans and machines alike to find out about any dataset the things that 'are there' and what properties they have. In combination with appropriate counting, agents can make highly informed decisions when exploring a knowledge graph - regardless whether e.g. a person searches for real estates with certain features or an algorithm that aims to prune its search space. Technologically, this is very similar.

However, besides the complexity related to query generation, a key question is: What performance can a SPARQL endpoint deliver for real faceted search queries? The answer depends on both the dataset AND the database management system in use.

To the best of our knowledge, this is the first schema agnostic faceted search benchmark generator for SPARQL-acessible knowledge graphs. 
By SPARQL-acessible we explicitly emphasize the possibility to use our system with data virtualization approaches, such as query federation, SPARQL-to-SQL rewriting, etc.


## Approach
The generator generates benchmarks against a given SPARQL endpoint. The benchmark itself is an RDF dataset that specifies multiple scenarios. A scenario is a sequence of batches, where a batch is a sequence of related SPARQL queries.

The rationale is as follows: A single user session corresponds to a scenario. After each transition in a session, conceptually, an application's views need to be updated with the latest facets, facet values and matching items - hence, these queries form a batch.


Theby simulating a faceted search session. Thereby, for each benchmark tasks from a library of faceted search transitions


The benchmark on Faceted Browsing aims to benchmark systems on their performance to support
browsing through linked data by iterative transitions performed by an intelligent user. By developing
realistic browsing scenarios through a dataset that comprises of different structural challenges for the
system, we aim to test its performance with respect to several choke points in a real world scenario.
# Prerequisites

For building, you need to have a working docker environment. Concretely, it is needed for the tests and for the integrated docker packaging of generated Java artifacts.


Make sure your user has access to the docker daemon (requires root).

```bash
sudo usermode -aG docker YOURUSERNAME
```

## Install docker images
At present, the benchmark will not mess around with your docker setup. This means it will also not pull images (and possibly cause unwanted updates).

```bash
docker pull tenforce/virtuoso
```

Install the images under (auxiliary-docker-resources)[auxiliary-docker-resources].
At present, you only need;

* podigg-lc-via-web-server


Now you should be good to perform your build with

```bash
mvn clean install
```


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


