package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

/**
 * 
 * A note on constraints:
 * Constraints can be distinguished by what they affect:
 * - NodeConstraint - a constraint on a BgpNode; disjunctive
 * - MultiNodeConstraint - a constraint on a BgpMultiNode; conjunctive
 * - GlobalConstraint - An arbitrary constraint, possibly affecting multiple paths
 * 
 * 
 * @author Claus Stadler, Sep 17, 2018
 *
 */
public interface FacetedQuery
	extends Castable
{
	FacetNode root();
	
//	SPath getFocus();
//	void setFocus(SPath path);

	FacetNode focus();
	void focus(FacetNode node);
	
	Concept toConcept();
	
	Collection<FacetConstraint> constraints();
	
	FacetedQuery baseConcept(Supplier<? extends UnaryRelation> conceptSupplier);
	FacetedQuery baseConcept(UnaryRelation concept);
	
	FacetedQuery connection(SparqlQueryConnection conn);
	SparqlQueryConnection connection();
	
	
	/**
	 * Lookup a facet node by id
	 * @param id
	 * @return
	 */
	//FacetNode find(Object id);
	
	
	//void UnaryRelation getBaseConcept();
}
