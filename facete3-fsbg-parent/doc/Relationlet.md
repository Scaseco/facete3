# Relationlets

Relationlets are snippets of (references to / names of) relations.
The term relationlet is a combination of relation and snippet, analoguous to applet (application and snippet).

## Motivation
Combining different parts of SPARQL graph patterns into a single coherent one is a non-trivial task, as demonstrated in the following example:

Given a specification of a set of resources, filter it by another specification and obtain the labels.

City mayor party
Base concept: ```?x { ?x a qb: }```
Attribute relation: ```?x ?y { ?y eg:humidity ?h ; eg:temperature ?t}
Sort condition: ```{ ?x timestamp ?timestamp }```

A relationlet is an abstraction over relations that only exposes column names or variables. Conversely, it does not expose the definition of the relation - whether it is a syntactic or algebraic expression.

## Predicate Resolver
Predicate resolvers provide the fundamental functionality for possibly stateful traversals of [basic property paths](Preliminaries#BasicPropertyPaths) with labels.
Resolvers serve the following two main purposes:

* Mapping of predicates to binary relations. In the simplest case, a predicate `:p` is mapped to the relations `?s ?o { ?s :p ?o }` and `?s ?o { ?o :p ?s }` for forward and backwards direction, respectively. However, this mechanism allows for supported custom logic for handling resolution of predicates.
* Stateful resolution; this allows custom resolution to only be applied for certain paths. This is used to resolve predicate paths over CONSTRUCT queries as will be shown later.


* Map predicate traversals to relationlets 
* Obtain a ternary relation that denotes the virtual RDF graph reachable after resolution of the labeled predicates. Note, that in the case of resolution over a CONSTRUCT template this is different from joining the path with ?s ?p ?o on either ?s or ?o. Hence, this functionality belongs at the resolver level, and not the higher one for joining relationlets.


```
interface Resolver {
    Resolver getParent();
    Resolver resolve(P_Path0 step, String alias);

    // The relationlet that corresponds to the resolution that reached this resolver
    // For the root node, this is a relationlet backed by an empty
    // group graph pattern with equal source and target variables
	Collection<RelationletBinary> getReachingRelationlet();

    // Obtain a specification of the RDF graph 
    Collection<TernaryRelation> getGraphSpec(boolean fwd);
}
```






 In general, they can be stateful, such that resolution of a predicate depends on prior resolutions.
Direction can be forwards or backwards.



## Unified API for Relations - Element and Op
```java
Relationlets.from(element);
Relationlets.from(op);
Pathlets.from(element);
Pathlets.from(op);
```

## Relationlet Joiner

