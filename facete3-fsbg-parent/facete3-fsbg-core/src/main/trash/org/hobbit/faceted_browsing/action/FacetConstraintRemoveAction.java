package trash.org.hobbit.faceted_browsing.action;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class FacetConstraintRemoveAction
	extends FacetConstraintBaseAction
{	
	public FacetConstraintRemoveAction(Node n, EnhGraph m) {
		super(n, m);
	}

	public Resource createUndoAction() {
		FacetConstraintAddAction result = new FacetConstraintAddAction(node, enhGraph);
		//result.setQuery(getQuery());
		result.setConstraint(getConstraint());
		return result;
	}
}
