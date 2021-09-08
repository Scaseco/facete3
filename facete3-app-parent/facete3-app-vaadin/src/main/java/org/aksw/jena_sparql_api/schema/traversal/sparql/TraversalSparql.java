package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.commons.path.trav.api.Trav;
import org.apache.jena.graph.Node;

public interface TraversalSparql<T extends TraversalSparql<T>>
    extends Trav<Node, T>
{
    QueryBuilder newQueryBuilder();
}
