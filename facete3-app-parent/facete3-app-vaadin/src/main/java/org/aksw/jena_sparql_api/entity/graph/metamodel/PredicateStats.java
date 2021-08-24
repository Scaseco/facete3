package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PredicateStats
    extends Resource
{
//    boolean isFwd();
//    QualifiedPredicateStats setFwd(boolean noOrYes);
//
//    String getPredicateIri();
//    QualifiedPredicateStats setPredicateIri(String iri);
//
//    Node getPredicate();
//    QualifiedPredicateStats setPredicate(Node node);
//
//
//    String getGraphIri();
//    QualifiedPredicateStats setGraphIri(String iri);
//
//    Node getGraph();
//    QualifiedPredicateStats setGraph(Node node);
//

    @IriNs("eg")
    @KeyIri("http://www.example.org/graph")
    Map<Node, GraphPredicateStats> getGraphToPredicateStats();

    default GraphPredicateStats getOrCreateStats(String key) {
        return getOrCreateStats(NodeFactory.createURI(key));
    }

    default GraphPredicateStats getOrCreateStats(Node key) {
        GraphPredicateStats result = getGraphToPredicateStats()
                .computeIfAbsent(key, k -> getModel().createResource().as(GraphPredicateStats.class));

        return result;
    }

}
