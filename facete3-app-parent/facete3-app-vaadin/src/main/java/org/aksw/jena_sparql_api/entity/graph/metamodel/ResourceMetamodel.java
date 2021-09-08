package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;


@HashId
@ResourceView
public interface ResourceMetamodel
    extends Resource
{
    @IriNs("eg")
    Resource getTargetResource();


    @HashId
    @IriType
    @IriNs("eg")
    String getTargetResourceIri();


    @IriNs("eg")
    @KeyIri("http://www.example.org/graph")
    Map<Node, ResourceGraphMetamodel> byGraph();


    // FIXME HashId cannot traverse into MapViews
    @Iri("http://www.example.org/byGraph")
    Set<ResourceGraphMetamodel> byGraphs();

    /** Whether the key set by byGraph covers all graphs w.r.t. an implicit context */
    @IriNs("eg")
    Boolean isGraphComplete();



    default ResourceGraphMetamodel getOrCreateResourceMetamodel(String iri) {
        return getOrCreateResourceMetamodel(NodeFactory.createURI(iri));
    }

    default ResourceGraphMetamodel getOrCreateResourceMetamodel(Node key) {
        ResourceGraphMetamodel result = byGraph()
                .computeIfAbsent(key, k -> getModel().createResource().as(ResourceGraphMetamodel.class));

        return result;
    }
}
