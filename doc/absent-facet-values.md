## Absent Facet Values

* Facets an facet values are the immediate forward/backward triples starting from a facetNode.
* For each facet of a facetNode, we can count the number of (distinct) focus resources that have no value for it.


Example Use Case / Category: Detect missing information
Funding -> Beneficiary -> Address -> City


Focus on fundings, show all that do not have a value for City.



Argument against absence on anywhere in the path:
If we were interested whether there are fundings with absent beneficaries, we could query so on the beneficiary predicate.


