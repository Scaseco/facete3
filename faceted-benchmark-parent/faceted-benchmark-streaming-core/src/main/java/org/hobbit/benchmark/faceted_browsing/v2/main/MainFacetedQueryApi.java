package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.vocabulary.RDF;

public class MainFacetedQueryApi {

	
	public static void main(String[] args) {
		new MainFacetedQueryApi().testSimpleFacetedQuery();
	}
	
	public void testSimpleFacetedQuery() {
		FacetedQuery fq = new FacetedQueryImpl();
		
		RDFConnection conn = null;
		fq
			.connection(conn)
			.baseConcept(Concept.create("?s a <http://www.example.org/ThingA>", "s"));
		
		
		// One .out() method moves along a given property
		// Another .out() method makes the api state head 'forth' or 'back' and the getFacetCounts method
		// then yields these facets
		
		
		// .getOutgoingFacets
		
		//fq.getRoot().out().getFacetsAndCounts();
		fq.root().fwd(RDF.type).one().bwd(RDF.type).availableValues();
		
		
		//fq.root().fwd(RDF.type).one().bwd(RDF.type).one().as("foobar");
		
			//.orderByCount()
//			.filter(null) //KeywordSearchUtils.createConceptBifContains( ))
//			.limit(10)
//			.orderBy()
//			.exec();
		
	}
}
