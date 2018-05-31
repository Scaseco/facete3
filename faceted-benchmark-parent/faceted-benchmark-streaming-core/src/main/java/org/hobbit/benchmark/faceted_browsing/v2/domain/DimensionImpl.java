package org.hobbit.benchmark.faceted_browsing.v2.domain;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class DimensionImpl
	extends AbstractResourceImpl
	implements Dimension
{
	public DimensionImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	protected Dimension parent;
	protected BinaryRelation relation;
	protected Var alias;
	
	
	@Override
	public Dimension getParent() {
		return parent;
	}

	@Override
	public Concept getValueConcept() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public Concept getOutgoingPredicatesConcept() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Concept getIncomingPredicatesConcept() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Dimension getPrimarySubDimension(String predicate, boolean isReverse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryRelation getReachingBinaryRelation() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public Dimension clone() {
//		Dimension result = new DimensionImpl();
//		return result;
//	}

//	@Override
//	public void registerVirtualPredicate(Node virtualPredicate, Dimension dimension) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void setAlias(Var alias) {
		this.alias = alias;
	}

	@Override
	public Var getAlias() {
		return alias;
	}

	@Override
	public boolean isReverse() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public SPath get(String predicate, boolean reverse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, BinaryRelation> getOutgoingFacets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, BinaryRelation> getIncomingFacets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPredicate() {
		// TODO Auto-generated method stub
		return null;
	}


}
