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


## Hobbit Faceted Browsing Benchmark V2
This is a schema agnostic benchmark that will perform faceted browsing interactions via SPARQL.

### Command Line Tooling
This benchmark comes with command line tools for data generation, benchmark generation and benchmark execution.


* facete3-fsbg `-c` config resource (file, classpath resource or URL - via Jena)
    * `config-tiny.ttl` - 1 scenario, 1 warmup, 3 tasks (with 3 queries each)
    * `config-all.ttl`
    * `config-no-ranges.ttl`


* Benchmark generation

Parameters
* White-or-blacklist of predicates to be (not) used in benchmarks
* Virtual predicates
* Random seed
* Number of scenarios
* Distribution of interactions within scenarios





* Benchmark execution





