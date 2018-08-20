package org.aksw.facete.v3.impl;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetNode;
import org.apache.jena.rdf.model.Resource;

public interface FacetNodeResource
	extends FacetNode
{
	FacetedQueryResource query();
	
	Resource state();
	FacetNodeResource parent();
	
	@Override
	ConstraintFacade<? extends FacetNodeResource> constraints();
}
