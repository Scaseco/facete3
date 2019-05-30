### Query Traversals

#### Introduction
This section demonstrates by example the purpose of the traversal API.
The essence of faceted queries over RDF is based on graph traversals via predicates and their aliases.
Traversals are closely related to SPARQL basic graph patterns.

For example, assuming one wants to traverse _specific_ properties of `?s` where `?s a foaf:Document` in a SPARQL query.
First, we need to decide on the *direction*: Forwards or Backwards

* `{ ?s :specificProperty ?target }`
* `{ ?target :specificProperty ?s }`

Once we have traversed the property, we end up reaching the set of its target variables. This set is initially empty, but the import aspect is, that in general there can be *multiple* variables.
For example, a query which asks for all publications having a certain combination of keywords requires one instance of a property for each keyword:

```
{
  ?s dct:keyword ?target_1, ?target_2
  FILTER(?target_1 = 'Big Data' && ?target_2 = 'Semantic Web')
}
```


Finally, there are 2 degrees of freedom in chosing the name of the `?target` variable: In complex situations, one may need to manage variable names manually, in simple ones a default generated one should be used.

Within the context of query generation, generated variables follow a get-or-create semantic - hence,
**Each path has only a single unique default generated variable.**.


#### The API

The three core classes, motivated by above example, are:

* `TraversalNode` Corresponds to a SPARQL variable in a graph pattern
* `TraversalDirNode` A TraversalNode with a direction attribute (forward / backward)
* `TraversalMultiNode` The set of SPARQL variables reached via a specific properties from a TraversalDirNode


Example: The line `traversalNode.fwd().via("foaf:knows").one()` can be broken down into

```
TraversalDirNode dn = traversalNode.fwd();
TraversalMultiNode mn = dn.via("foaf:knows");
TraversalNode tn = mn.one()
```

and translates to a basic graph pattern, where a default for the target variable is used.
```
?root foaf:knows ?genid
```



