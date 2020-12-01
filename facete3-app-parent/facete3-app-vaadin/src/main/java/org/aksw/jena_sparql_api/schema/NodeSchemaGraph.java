package org.aksw.jena_sparql_api.schema;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.facete3.app.shared.concept.NodeSpec;
import org.aksw.jena_sparql_api.collection.ObservableSet;
import org.aksw.jena_sparql_api.collection.ObservableValue;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.RDFConnection;

import com.google.common.collect.Multimap;


/**
 * A field that once a value is set registers an intensional deletion of any other potentially
 * matching tripl at the underlying model.
 *
 * @author raven
 *
 */
//class NodeField
//    extends ObservableValue<Node>
//{
//    protected NodeSchemaGraph graph;
//
//    protected Node sourcNode;
//    protected PropertySchema propertySchema;
//
//
//    void setNode(Node node) {
//
//    }
//
//    public Node getNode() {
//
//    }
//}

public class NodeSchemaGraph {
    // For each node keep track of the nodeSpec it was referenced by.
    protected Multimap<NodeSchema, NodeSpec> schemaAndNodeSpec;
    protected RDFConnection conn;

    protected Graph cache;

    public ObservableSet<Node> getSet(Node predicate, boolean isFwd) {
        return null;
    }

    public SingleValuedAccessor<Node> getField(Node predicate, boolean isFwd) {
        return null;
    }



}


