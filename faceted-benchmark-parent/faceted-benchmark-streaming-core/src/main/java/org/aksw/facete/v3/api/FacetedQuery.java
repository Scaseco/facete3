package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public interface FacetedQuery {
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
	
	
	//void UnaryRelation getBaseConcept();
}
