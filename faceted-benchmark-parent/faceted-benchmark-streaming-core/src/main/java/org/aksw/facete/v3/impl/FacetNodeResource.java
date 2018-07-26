package org.aksw.facete.v3.impl;

import org.aksw.facete.v3.api.FacetNode;
import org.apache.jena.rdf.model.Resource;

public interface FacetNodeResource
	extends FacetNode
{
	Resource state();
	FacetNodeResource parent();
}
