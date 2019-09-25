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
Here a teaser of the terminal application on [Scholarly Data](http://www.scholarlydata.org/)'s [SPARQL endpoint](http://www.scholarlydata.org/sparql/):

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

Please refer to the respective Facete 3 component READMEs for details about how to use them.


## Licence
The source code of this repo is published under the [Apache License Version 2.0](LICENSE).
Dependencies may be licenced under different terms. When in doubt please refer to the licences of the dependencies declared in the pom.xml files.
