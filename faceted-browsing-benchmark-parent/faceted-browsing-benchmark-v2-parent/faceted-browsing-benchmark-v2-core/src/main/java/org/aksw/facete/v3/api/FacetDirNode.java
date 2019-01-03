package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.rdf.model.RDFNode;

public interface FacetDirNode extends DirNodeNavigation<FacetMultiNode> {
	/** The parent of this node, should never be null */
	FacetNode parent();

	/** Get the query object this node belongs to */
	default FacetedQuery query() {
		FacetedQuery result = parent().query();
		return result;
	}
	
	/** The set of all outgoing edges - similar corrensponds to the triple ?s ?p ?o */
	//FacetEdgeSet out();

//	void as(Var var);
//	Var getAlias();
	

	/** The relation of facet and facet value */
	// TODO We may want to make this a default method that derives the relation from\
	// a ternary focus, facet, value relation
	BinaryRelation facetValueRelation();
		
	/** Facets without counts, i.e. just the available predicates */
	DataQuery<RDFNode> facets(boolean includeAbsent);

	default DataQuery<RDFNode> facets() {
		return facets(false);
	}

	// Get the facets of this set of values with count of their distinct values
	//Collection<FacetCount> getFacetsAndCounts();
	/** Facets and counts **/
	DataQuery<FacetCount> facetCounts(boolean includeAbsent);
	
	default DataQuery<FacetCount> facetCounts() {
		return facetCounts(false);
	}
	
	
	default FacetValueQueryBuilder<? extends FacetValue> facetValues() {
		throw new RuntimeException("This should become the new API for facetValues - but its only a stub yet");
	}
	
	// Get the facets of this set of values with the counts referring the the query's focus
	@Deprecated
	DataQuery<FacetValueCount> facetValueCounts();

	/**
	 * This method is an API hack to get the feature in now (without breaking everything)
	 * It should be replaced with a "FacetValueBuilder facetValues()" method
	 * 
	 * @return
	 */
	@Deprecated
	DataQuery<FacetValueCount> facetValueCountsWithAbsent(boolean includeAbsent);

	
	/** Yield all facet value counts NOT affected by filters -
	 *  So each item can be used as a fresh filter */
	DataQuery<FacetValueCount> nonConstrainedFacetValueCounts();
	
	//ExprFragment2 constraintExpr();
	//FacetMultiNode out(Path propertyPath);
	
	
	
//	default FacetNode root() {
//		FacetNode parent = parent();
//		FacetNode result = parent == null ? this : parent.root();
//		return result;
//	}

//	default void as(String name) {
//		Var var = Var.alloc(name);
//	    as(var);
//	}
	
}


///**
// * An abstraction of a set of edges
// * 
// * @author Claus Stadler, Jul 23, 2018
// *
// */
//interface FacetEdgeSet {
//	FacetEdgeSet filter(Concept c);
//	FacetEdgeSet order();
//	
//	// session.root().get("somePredicate").outgoing()
//	//                   .out(somePredicate)
//	// orderByCount
//	// orderByIRI();
//	// orderByLabel() - default labels
//	// orderByAttribute(BinaryRelation)
//	
//	// Return the facets (properties) with their distinct value counts
//	void getFacetCounts();
//}
//
//interface FacetEdge {
//
//}