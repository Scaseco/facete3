package org.aksw.facete.v3.impl;

import org.aksw.facete.v3.api.FacetedQuery;
import org.apache.jena.rdf.model.Resource;

public interface FacetedQueryResource
	extends FacetedQuery
{
	Resource modelRoot();
}
