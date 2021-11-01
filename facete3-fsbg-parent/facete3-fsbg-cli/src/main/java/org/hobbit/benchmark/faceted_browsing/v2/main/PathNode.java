package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Map;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.utils.views.map.MapFromKeyConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapFromResourceUnmanaged;
import org.aksw.jena_sparql_api.utils.views.map.MapFromValueConverter;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import com.google.common.base.Converter;

public interface PathNode
    extends Resource
{
    public static final Property DEPTH = ResourceFactory.createProperty("http://www.example.org/depth");
    public static final Property TRANSITIONS = ResourceFactory.createProperty("http://www.example.org/transition");
    public static final Property PREDICATE = ResourceFactory.createProperty("http://www.example.org/predicate");

    @IriNs("eg")
    Long getDepth();

    @IriType
    @IriNs("eg")
    String getPredicate();

    @IriNs("eg")
    Long getCount();

    default Map<Resource, PathNode> getTransitions() {
        Map<RDFNode, Resource> map = new MapFromResourceUnmanaged(this, TRANSITIONS, PREDICATE);

        Map<Resource, Resource> m = new MapFromKeyConverter<>(map, Converter.from(r -> r.as(Resource.class), RDFNode::asResource));
        Map<Resource, PathNode> result = new MapFromValueConverter<>(m, Converter.from(r -> r.as(PathNode.class), RDFNode::asResource));

        return result;
    }


    // Returns this nodes count plus the counts of all its children
    default long getTotalCount() {
        long result = getCount();
        for(PathNode pn : getTransitions().values()) {
            long contrib = pn.getTotalCount();
            result += contrib;
        }

        return result;
    }
}
