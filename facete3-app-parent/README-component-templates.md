
## Presentation of entities

Entities are concrete RDF terms (IRIs, literals or blank nodes).
Entities have an identity. In the case of IRIs and literals an entity's identity is itself, however in the case of blank nodes it is a mapping to a
composite key.


* An entity key is a sequence of variables, such as <?s> or <?city ?time>


Ordered sets of entities are intensionally described using a sparql query whose projected variables match those of an entity key pattern.


?x ?y {
  { SELECT ?x ?y (COUNT(*) AS ?c) {
    ?x ... ?y ... ?n
  } GROUP BY ?x ?y HAVING(COUNT(*) > 10) ORDER BY ASC(MIN(?n))
}


## Registration of Conditions for the Classification of Entities

The EntityClassifier class allows one register conditions on entities. Each condition is linked to an RDF term that reprsents it.
Given a relation of entities, the classifier returns a new relation that has an extra classifier column.
The classifier takes care of appropriate variable renaming.
The syntax for `Concept.parse` is that of SPARQL SELECT queries, however the SELECT keyword can be omitted.
This allows the notion `?s { ?s satisfies conditions }` which resembles the usual
mathematical notion of `{ s | s safisfies conditions }`


```java
EntityClassifier entityClassifier = new EntityClassifier(Arrays.asList(Vars.s));
entityClassifier.addCondition(NodeFactory.createURI("urn:satisfies:hasLabel"),
        Concept.parse("?s { ?s <urn:has:label> ?l }"));

entityClassifier.addCondition(NodeFactory.createURI("urn:satisfies:HasType"),
        Concept.parse("?x { ?x a ?t }"));

EntityGraphFragment r = entityClassifier.createGraphFragment();

System.out.println(r);
```

```sparql
ENTITY [?s]
CONSTRUCT {urn:x-arq:DefaultGraphNode=(?s <http://jsa.aksw.org/classifier> ?conditionId_1)}, entity nodes [?s], bnodeIdMapping {}
WHERE   { ?s  <urn:has:label>  ?l
    BIND(<urn:satisfies:hasLabel> AS ?conditionId_1)
  }
UNION
  { ?s  a  ?t
    BIND(<urn:satisfies:HasType> AS ?conditionId_1)
  }
```

The resulting entity graph fragment can be used to exend SPARQL queries to 'annoted' entities with additional properties.
In this case, the classifier extends the entities with the appropriate classes.

## Using the Classifier
The classifier can create a relation that maps entities to their tags, what is needed is to specify the set of entities for which to run the classification.


### Entity Queries

An entity query uses a standard SPARQL query as a base and adds information about which sequence of variables corresponds to the key of the entities matched in the WHERE pattern. This extension to the SPARQL model allows for the specification of ordered sets of entities.
```
CONSTRUCT { _:X .. } # a construct template based on the graph pattern in entities (avoids having to duplicate it if it should also participate in a construct)
ENTITIES ?x ?y { ?x . ?y . ?name }
ORDER ENTITIES BY ASC(MIN(?name))
OFFSET 5 # Start with the 5th entity - not the 5ths binding
LIMIT 10 # Match 10 entities - not the 10th binding

\# Addition of graph fragement to fetch additional information for the entities

ENTITY _:X # The element in the template (which may# be empty) or where pattern that represents the entities
KEY _:X (?x ?y) # A short hand for BIND(BNODE(CONCAT(STR(?x), STR(?y))) AS ?X)
CONSTRUCT { } WHERE { ?x classification  }

```









