package org.aksw.jena_sparql_api.schema.traversal.rdf;

import org.aksw.jena_sparql_api.schema.traversal.api.Trav;
import org.apache.jena.graph.Node;

public interface TraversalNode<T extends TraversalNode<T>>
    extends Trav<Node, T>
{
//    @Override
//    PathNode getPath();
}
