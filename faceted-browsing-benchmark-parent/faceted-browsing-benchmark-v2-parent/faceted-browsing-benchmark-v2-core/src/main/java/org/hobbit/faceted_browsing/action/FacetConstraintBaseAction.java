package org.hobbit.faceted_browsing.action;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.impl.FacetedQueryResource;
import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class FacetConstraintBaseAction
	extends ResourceBase
{
	public FacetConstraintBaseAction(Node n, EnhGraph m) {
		super(n, m);
	}

	public void setQuery(Resource query) {
		ResourceUtils.setProperty(this, Vocab.query, query);
	}

	public FacetedQueryResource getQuery() {
		return ResourceUtils.getPropertyValue(this, Vocab.query, FacetedQueryResource.class).orElse(null);
	}
	
	public FacetConstraint getConstraint() {
		return ResourceUtils.getPropertyValue(this, Vocab.constraint, FacetConstraint.class).orElse(null);
	}

	public void setConstraint(Resource constraint) {
		ResourceUtils.setProperty(this, Vocab.constraint, constraint);
	}

}
