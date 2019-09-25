## Hobbit Faceted Browsing Benchmark V2
This is a schema agnostic benchmark that will perform faceted browsing interactions via SPARQL.

### Command Line Tooling
This benchmark comes with command line tools for data generation, benchmark generation and benchmark execution.


* `-c` config resource (file, classpath resource or URL - via Jena)
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





