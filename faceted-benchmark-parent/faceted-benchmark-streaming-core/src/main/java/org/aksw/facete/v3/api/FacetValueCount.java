package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.utils.CountInfo;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public interface FacetValueCount
	extends Resource
{
	//String getPredicate();
	
	Node getValue();
	CountInfo getFocusCount();
}
