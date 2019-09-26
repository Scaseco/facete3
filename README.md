# Facete3 Faceted Search Framework

Facete is a faceted search framework for SPARQL-accessible data. We are working on aggregeations, so actually its becoming more of a SPARQL-based business intelligence system.

A brief history
* Facete 1 was probably the first pure production JavaScript SPARQL-based faceted search system deployed at the European Open Dataportal at around 2012
* Facete 2 was an re-implementation based on Angular 1 (JavaScript) at around 2015
* Facete 3 is the current iteration which finally got large parts of the API just right - this time its Java.

## Factete 3 Components

The project comprises the following component:

* [The core API](facete3-core-parent)
* [A terminal application](facete3-core-parent) (currently part of core, may be moved to a separate module)
* [A faceted search benchmark generator](facete3-fsbg-parent). Benchmark results are published in [this repository](https://github.com/hobbit-project/facete3-fsbg-results)!


## Teasers
Here are a few teasers to give you an impression of the project before you read on.

A screenshot of the Facete3 terminal application on [Scholarly Data](http://www.scholarlydata.org/)'s [SPARQL endpoint](http://www.scholarlydata.org/sparql/):

![Screenshot](doc/2019-09-25-Facete3-TerminalApp.png)

And here a teaser for what the Facete3 core API looks like - reactive streams powered by [RxJava2](https://github.com/ReactiveX/RxJava):

```java
class TestFacetedQuery {
    @Test
    public void testComplexQuery() {
        RDFConnection conn = RDFConnectionFactory.connect(someDataset);
        FacetedQuery fq = fq = FacetedQueryImpl.create(conn);

        FacetValueCount fc =
                // -- Faceted Browsing API
                fq.root()
                .fwd(RDF.type).one()
                    .constraints()
                        .eq(OWL.Class).activate()
                    .end()
                .parent()
                .fwd()
                .facetValueCounts()    
                // --- DataQuery API
                //.sample()
                .randomOrder()
                .limit(1)
                .exec()
                // --- RxJava API
                .firstElement()
                .timeout(10, TimeUnit.SECONDS)
                .blockingGet();

        System.out.println("FacetValueCount: " + fc);
    }
}
```

## Building

This project uses Apache Maven and is thus built with:

```bash
mvn clean install
```

* The Facete3 bundle is built under `facete3-bundle/target/facete3-bundle-VERSION-jar-with-dependencies.jar` with `VERSION` matching the project version. The bundles are also available for download from the [Releases Section](https://github.com/hobbit-project/faceted-browsing-benchmark/releases).
* Debian packages are built under `facete3-core-parent/facete3-debian-cli` and `facete3-fsbg-parent/facete3-fsbg-debian-cli`. Because they share most of the code, we will combine them into a single one.



## Running the bundle


* The Facete3 Terminal App
```
# Show help
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar facete3 --help

# Run against a local file, remote RDF document or SPARQL endpoint
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar facete3 http://www.w3.org/1999/02/22-rdf-syntax-ns#

# For installation from the debian package, the command is
facete3 --help
```


* Faceted Search Benchmark Generator (fsbg)
```
# Show help
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar fsbg --help

# Generate a benchmark using default settings
# against a given SPARQL endpoint
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar fsbg http://localhost:8890/sparql


# Generate a benchmark using alternative config (class path or file)
# against a given SPARQL endpoint
# Note: config-tiny.ttl is part of the classpath
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar fsbg -c config-tiny.ttl http://localhost:8890/sparql

# For installation from the debian package, the command is
facete3-fsbg --help
```

Please refer to the respective Facete 3 component READMEs for details about how to use them.


## Licence
The source code of this repo is published under the [Apache License Version 2.0](LICENSE).
Dependencies may be licenced under different terms. When in doubt please refer to the licences of the dependencies declared in the pom.xml files.
