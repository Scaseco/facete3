package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * Information about
 *
 * @author raven
 *
 */
@ResourceView
public interface ResourceMetamodel
    extends Resource
{
//  Capture the total number of predicates? Probably its more useful to have to concrete list of known predicates from
//  which the count can be derived
//	@IriNs("eg")
//	Long getOutgoingPredicateConut();
//	@IriNs("eg")
//	Long getIngoingPredicateCount();


    @IriNs("eg")
    Set<Node> getKnownOutgoingPredicates();

    @Iri("eg:knownOutgoingPredicates")
    @IriType
    Set<Node> getKnownOutgoingPredicateIris();

    @IriNs("eg")
    boolean isAllOutgoingPredicatesKnown();
    ResourceMetamodel setAllOutgoingPredicatesKnown(boolean noOrYes);

    @IriNs("eg")
    @KeyIri("urn:predicate")
    Map<Node, PredicateStats> getOutgoingPredicateStats();


    @IriNs("eg")
    Set<Node> getKnownIngoingPredicates();

    @Iri("eg:knownOutgoingPredicates")
    @IriType
    Set<String> getKnownIngoingPredicateIris();

    @IriNs("eg")
    boolean isAllIngoingPredicatesKnown();
    ResourceMetamodel setAllIngoingPredicatesKnown(boolean noOrYes);

    @IriNs("eg")
    @KeyIri("urn:predicate")
    Map<Node, PredicateStats> getIngoingPredicateStats();


    default PredicateStats getOrCreateOutgoingPredicateStats(String key) {
        return getOrCreateOutgoingPredicateStats(NodeFactory.createURI(key));
    }

    default PredicateStats getOrCreateOutgoingPredicateStats(Node key) {
        PredicateStats result = getOutgoingPredicateStats()
                .computeIfAbsent(key, k -> getModel().createResource().as(PredicateStats.class));

        return result;
    }


    default PredicateStats getOrCreateIngoingPredicateStats(String key) {
        return getOrCreateIngoingPredicateStats(NodeFactory.createURI(key));
    }

    default PredicateStats getOrCreateIngoingPredicateStats(Node key) {
        PredicateStats result = getIngoingPredicateStats()
                .computeIfAbsent(key, k -> getModel().createResource().as(PredicateStats.class));

        return result;
    }
}
