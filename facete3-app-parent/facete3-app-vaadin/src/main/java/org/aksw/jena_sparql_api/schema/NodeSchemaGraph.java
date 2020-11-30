package org.aksw.jena_sparql_api.schema;

import org.aksw.facete3.app.shared.concept.NodeSpec;
import org.apache.jena.rdfconnection.RDFConnection;

import com.google.common.collect.Multimap;

public class NodeSchemaGraph {
    // For each node keep track of the nodeSpec it was referenced by.
    protected Multimap<NodeSchema, NodeSpec> schemaAndNodeSpec;
    protected RDFConnection conn;






}
