## Facete3 Terminal App

![Screenshot](../../doc/2019-09-25-Facete3-TerminalApp.png)


## Running Facete3 from the bundle

```bash
# Show help
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar facete3 --help

# Run against a local file, remote RDF document or SPARQL endpoint
java -cp facete3-bundle-VERSION-jar-with-dependencies.jar facete3 http://www.w3.org/1999/02/22-rdf-syntax-ns#

# For installation from the debian package, the command is
facete3 --help

# Browse a set of files (note: mixing triple and quad based formats is not recommended, see comments on the -u option)
facete3 file.nt file.ttl file.rdf file.hdt

# Browse a sparql endpoint
facete3 https://databus.dbpedia.org/repo/sparql

```

### Advanced invocations

```bash
# Providing a default graph and a base concept (must be a select query with exactly one result variable)
# Note, that most of the loading time at present is due to label fetching - we intent to improve this with a better caching system.
facete3 -g http://dbpedia.org -c 'SELECT ?s { ?s rdfs:label ?l . ?l <bif:contains> "Potter" }' http://dbpedia.org/sparql
```

```
# Browse data in named graphs - quad based formats are sometimes used to group triples that make up an event in an RDF stream
facete3 -u observations1.trig observations2.trig
```

## Features

* Navigation along nested properties
* Exact facet /facet value counts
* Sorting of facets and facet values by counts
* RDF1.0 and RDF1.1 supported (powered by Jena's legacy mode, tested on DBpedia databus)
* HDT support
* Union default graph query rewriting (-u option)
* Automatic SPARQL endpoint DBMS probing / detection and blank node query rewriting strategy selection (does not yet work for RDF4J based systems)


### Notes

* All named graphs of a dataset can be viewed as a single graph using the -u option, such as in `facete3 -u data.trig`.
  * Without the option, only the content of the default graph will be visible. With this option, the default graph will **NOT** be visible.


## Keys

* **Arrow Keys** moves between individual items
* **Tab** and **Shift+Tab** jumps forward / backwards between element groups
* **Spacebar** shows the triples of the currently selected item (regardless of facet, facet value or result table list)
* **Enter** activates facets and toggles constraints
* **Backspace** on a facet navigates along the facet
* **s** context-sentively (s)hows the SPARQL query that returned the currently highlighted item in the facet, facet value or result table list.
* **q** or **ESC** quits (asks for confirmation)
* **Ctrl+Mouse** elect a rectangle (useful to copy output of **s**)


## Upcoming

* Start browsing from a limited set of resources, such as keyword search using `facete3 -c '{ ?s bif:contains "Awesome" }`
* Mark items for selection in order to export the items or the underlying query
* Support for changing the focus of the matching items table
* Allow showing related properties as extra columns to any view.
* Aggregations


