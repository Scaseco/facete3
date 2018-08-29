package org.aksw.facete.v3.impl;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.bgp.api.BgpNode;

public interface FacetNodeResource
	extends FacetNode
{
	FacetedQueryResource query();
	
	BgpNode state();
	FacetNodeResource parent();
	
	@Override
	ConstraintFacade<? extends FacetNodeResource> constraints();
}
