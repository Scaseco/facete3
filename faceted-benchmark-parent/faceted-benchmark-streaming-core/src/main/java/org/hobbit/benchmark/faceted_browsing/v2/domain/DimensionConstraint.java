package org.hobbit.benchmark.faceted_browsing.v2.domain;

import java.util.Set;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

/**
 * A resource that represents a constraint expression over one or more dimensions.
 * 
 * TODO Do we need ownership of constraints?
 * Constraints can be owned by
 * - a dimension (e.g. rdf:type = lgdo:Airport)
 * - a query (?)
 * 
 * @author Claus Stadler, May 30, 2018
 *
 */
public class DimensionConstraint
	extends AbstractResourceImpl
{
	public DimensionConstraint(Node n, EnhGraph m) {
		super(n, m);
	}
	
	
	public Set<Dimension> getAffectedDimensions() {
		return null;
	}
}
