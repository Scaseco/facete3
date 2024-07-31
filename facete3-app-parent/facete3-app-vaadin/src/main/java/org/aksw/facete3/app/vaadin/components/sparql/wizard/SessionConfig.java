package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefSparqlEndpoint;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@Namespace("http://www.example.org/")
public interface SessionConfig {
    @IriNs
    DataRefSparqlEndpoint getEndpoint();
    SessionConfig setEndpoint(Resource endpoint);

    // Perhaps we need more info than just the classes
    @IriNs
    List<Node> getPolyfillClasses();

    @IriNs
    List<Node> getTypes();
}
