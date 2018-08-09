package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.impl.FacetedQueryResource;
import org.aksw.facete.v3.impl.PathAccessorImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.DatasetAnalyzerRegistry;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.HierarchyCoreOnDemand;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.TaskGenerator;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.ConceptAnalyser;

public class MainFacetedQueryApi {

	
	public static void main(String[] args) {
		new MainFacetedQueryApi().testSimpleFacetedQuery();
	}
	
	public void testSimpleFacetedQuery() {
		FacetedQueryResource fq = new FacetedQueryImpl();
		

		
		Model m = RDFDataMgr.loadModel("path-data.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create(m));		


		
		ReactiveSparqlUtils.execSelect(() -> 
			conn.query("" + ConceptUtils.createQueryList(HierarchyCoreOnDemand.createConceptForRoots(PathFactory.pathLink(RDFS.subClassOf.asNode())))))
			.toList().blockingGet().forEach(x -> System.out.println("Reverse Root: " + x));

		
		
		
//		ReactiveSparqlUtils.execSelect(() -> 
//		conn.query("SELECT DISTINCT ?root {\n" + 
//				"  [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root\n" + 
//				"  FILTER(NOT EXISTS { ?root (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)+ ?ancestor . FILTER(NOT EXISTS {?ancestor <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent }) . })\n" + 
//				"}")).toList().blockingGet().forEach(x -> System.out.println("Reverse Root: " + x));

//		ReactiveSparqlUtils.execSelect(() -> 
//		conn.query("SELECT ?root {\n" + 
//				"  [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root\n" + 
//				"    FILTER(NOT EXISTS { ?root (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)* ?ancestor . ?ancestor <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . FILTER(?root = ?ancestor) . })\n" + 
//				"}")).toList().blockingGet().forEach(x -> System.out.println("Reverse Root: " + x));

		System.out.println("Done listing roots");
		fq
			.connection(conn)
			.baseConcept(Concept.create("?s a <http://www.example.org/ThingA>", "s"));
		
		
		
//		ReactiveSparqlUtils.execSelect(() -> 
//		conn.query("SELECT  *\n" + 
//				"WHERE\n" + 
//				"  { { ?v_1  a                     ?v_2 . \n" + 
//				"      ?v_2  a                     ?v_3\n" + 
//				"      FILTER ( ?v_3 = <http://www.w3.org/2002/07/owl#Class> )\n" + 
//				"    }\n" + 
//				"    ?v_1  ?p  ?o\n" + 
//				"  }\n" + 
//				"")).toList().blockingGet().forEach(x -> System.out.println("Item: " + x));
		// One .out() method moves along a given property
		// Another .out() method makes the api state head 'forth' or 'back' and the getFacetCounts method
		// then yields these facets
		
		
		// .getOutgoingFacets
		
		//fq.getRoot().out().getFacetsAndCounts();
		//fq.root().fwd(RDF.type).one().as("test").availableValues();
		
		
//		FacetNode facetNode = fq.root().fwd(RDF.type).one()
//			.fwd(RDF.type).one()
//				.constraints()
//					.eq(OWL.Class.asNode())
//				.end();
		
//		FacetNode facetNode = fq.root().fwd(RDF.type).one()
//			.constraints()
//				.eq(NodeFactory.createURI("http://www.example.org/ThingA"))
//			.end();

//		FacetNode facetNode = fq.root().fwd("http://www.opengis.net/ont/geosparql#geometry").one()
//				.constraints()
//					.exists()
//				.end();

		//TaskGenerator.applyCp1(facetNode);
		
		//TaskGenerator.applyCp4(fq.root());

		TaskGenerator taskGenerator = TaskGenerator.configure(conn);
		taskGenerator.applyCp6(fq.root());

		
		//ConceptPathFinder.
		
		//fq.root().fwd().facetValueCounts();
		
		if(false) { 
//		System.out.println("Available values: " + facetNode.availableValues().exec().toList().blockingGet());
//		System.out.println("Remaining values: " + facetNode.remainingValues().exec().toList().blockingGet());
		
		System.out.println("Test: " + new PathAccessorImpl(fq).isReverse(fq.root().fwd(RDF.type).one()));
		System.out.println("Test: " + new PathAccessorImpl(fq).isReverse(fq.root().bwd(RDF.type).one()));
		
		FacetedQueryGenerator<FacetNode> qgen = new FacetedQueryGenerator<FacetNode>(new PathAccessorImpl(fq));
		
		fq.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));
//		fq.constraints().forEach(c -> qgen.getConstraints().add(c.expr()));
		//qgen.getConstraints()
		
		System.out.println("Query Fwd: " + qgen.getFacets(fq.root().fwd(RDF.type).one(), false, false));
		System.out.println("Query Bwd: " + qgen.getFacets(fq.root().fwd(RDF.type).one(), true, false));
		
		//fq.root().fwd(RDF.type).one().constraints().eq("foo").addEq("bar").end()

	
		
		// Test whether we get the correct alias
		System.out.println("Alias: " + fq.root().fwd(RDF.type).one().alias());
		}
		
		//fq.root().fwd(RDF.type).one().bwd(RDF.type).one().as("foobar");
		
			//.orderByCount()
//			.filter(null) //KeywordSearchUtils.createConceptBifContains( ))
//			.limit(10)
//			.orderBy()
//			.exec();
		
	}
}
