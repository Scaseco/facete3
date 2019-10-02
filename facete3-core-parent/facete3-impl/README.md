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
```


## Keys

* **Spacebar** shows the triples of the currently selected item (regardless of facet, facet value or result table list)
* **Enter** activates facets
* **q** or **ESC** quits (asks for confirmation)
* **Backspace** on a facet navigates along the facet


## Upcoming

* Mark items for selection in order to export the items or the underlying query
* Support for changing the focus of the matching items table

## Needs more time

* Sorting by counts (its implemented in the API, but not well tested yet)
* Allow showing related properties as extra columns to any view.
* Aggregations

