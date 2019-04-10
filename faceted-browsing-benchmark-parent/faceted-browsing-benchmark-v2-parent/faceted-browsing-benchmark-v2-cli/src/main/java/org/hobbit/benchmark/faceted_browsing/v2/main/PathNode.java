package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Map;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.utils.views.map.MapFromProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public interface PathNode
	extends Resource
{
	public static final Property TRANSITIONS = ResourceFactory.createProperty("http://www.example.org/transitions");
	public static final Property PREDICATE = ResourceFactory.createProperty("http://www.example.org/predicate");
	
	@IriNs("eg")
	Long getDepth();
	
	@IriType
	@IriNs("eg")
	String getPredicate();

	@IriNs("eg")
	Long getCount();
	
	default Map<RDFNode, Resource> getTransitions() {
		return new MapFromProperty(this, TRANSITIONS, PREDICATE);
	}
}
