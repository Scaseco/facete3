### Faceted Benchmark Core

This module contains all java code and bindings to run the faceted benchmark.

In order to exploit the functionality of this module to its fullest, the following components must be present on your system:

* The podigg executable: Podigg is a JavaScript tool that does the actual data generation.
* A Virtuoso triple store installation: This is required for task generation. In the future, this may become obsolete by using an embedded triple store for this purpose, but as of now, task generation makes use of virtuoso specific features.


Setup of these components can be done via system variables:

* PODIGG_HOME="" (no default value)

* VIRTUOSO_BIN="/usr/local/virtuoso-opensource/bin/"
* VIRTUOSO_DB="/"


* VIRTUOSO_DB_URL="http://localhost:1111/sparql"
* VIRTUOSO_USER="dba"
* VIRTUOSO_PASS="dba"
* VIRTUOSO_SPARQL_ENDPOINT="http://localhost:8890/sparql"




