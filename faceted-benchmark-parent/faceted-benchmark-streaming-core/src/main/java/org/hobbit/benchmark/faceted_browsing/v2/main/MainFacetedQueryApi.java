package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.impl.PathAccessorImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.expr.NodeValue;
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
		fq.root().fwd(RDF.type).one().as("test").bwd(RDF.type).availableValues();
		
		
		fq.root().fwd(RDF.type).one().as("test")
			.constraints()
				.eq(NodeValue.makeInteger(5).asNode())
			.end();
		
		
		System.out.println("Test: " + new PathAccessorImpl().isReverse(fq.root().fwd(RDF.type).one()));
		System.out.println("Test: " + new PathAccessorImpl().isReverse(fq.root().bwd(RDF.type).one()));
		
		FacetedQueryGenerator<FacetNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl());
		
		System.out.println("Query Fwd: " + qgen.getFacets(fq.root().fwd(RDF.type).one(), false));
		System.out.println("Query Bwd: " + qgen.getFacets(fq.root().fwd(RDF.type).one(), true));
		
		//fq.root().fwd(RDF.type).one().constraints().eq("foo").addEq("bar").end()

	
		
		// Test whether we get the correct alias
		System.out.println("Alias: " + fq.root().fwd(RDF.type).one().alias());
		
		
		//fq.root().fwd(RDF.type).one().bwd(RDF.type).one().as("foobar");
		
			//.orderByCount()
//			.filter(null) //KeywordSearchUtils.createConceptBifContains( ))
//			.limit(10)
//			.orderBy()
//			.exec();
		
	}
}
