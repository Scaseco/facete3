package org.aksw.jena_sparql_api.collection;

import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

/**
 * A sub graph view for a designated node that acts as a starting point
 * for traversal of triples.
 *
 * @author raven
 *
 */
public class NodeGraphView
    extends SubGraphView
{
    protected Node source;
    protected NodeSchema schema;

    public NodeGraphView(Graph graph, Node source, NodeSchema schema) {
        super(graph);
        this.source = source;
        this.schema = schema;
    }



//    protected Map<Node, PropertyGraphSpec> propertyViews;

//    protected Set<DirectedFilteredTriplePattern> edges;

    public NodeGraphView(Graph graph) {
        super(graph);
    }


}
