package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@HashId
@ResourceView
public interface RGDMetamodel
    extends Resource
{
    @HashId
    @Inverse
    @Iri("urn:fwd")
    ResourceGraphMetamodel getFwdRef();

    @HashId
    @Inverse
    @Iri("urn:bwd")
    ResourceGraphMetamodel getBwdRef();


    @IriNs("eg")
    Boolean isPredicateComplete();

    @IriNs("eg")
    @KeyIri("http://www.example.org/predicate")
    Map<Node, PredicateStats> getPredicateStats();



    // FIXME HashId lacks feature to descend into map views
    @Iri("http://www.example.org/predicateStats")
    Set<PredicateStats> getStats();
}
