package org.hobbit.faceted_browsing.action;

import org.aksw.facete.v3.api.FacetedQuery;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class FacetConstraintAddAction
	extends FacetConstraintBaseAction
{	
	public FacetConstraintAddAction(Node n, EnhGraph m) {
		super(n, m);
	}

	public Resource createUndoAction() {
		FacetConstraintRemoveAction result = new FacetConstraintRemoveAction(node, enhGraph);
		//result.setQuery(getQuery());
		result.setConstraint(getConstraint());
		return result;
	}
	
	public void apply() {
		FacetedQuery fq = null;
		fq.constraints().add(getConstraint());
	}
}
