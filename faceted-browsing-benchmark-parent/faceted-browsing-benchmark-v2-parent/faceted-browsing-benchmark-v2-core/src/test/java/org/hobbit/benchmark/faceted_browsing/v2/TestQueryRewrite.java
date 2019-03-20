package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.function.Function;

import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantFilterRemoval;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.junit.Assert;
import org.junit.Test;


public class TestQueryRewrite {
// PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
// SELECT DISTINCT ?p {
//     {}
//   UNION
//  { ?s rdf:type ?o
//    BIND(rdf:type AS ?p)
//    FILTER (?p IN (rdf:type))
//  }
//
	@Test
	public void testRewriteRedundantFilterAfterBindInUnion1() {
		Query input = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT * {\n" + 
				"     {}\n" + 
				"   UNION\n" + 
				"  { ?s rdf:type ?o\n" + 
				"    BIND(rdf:type AS ?p)\n" + 
				"    FILTER (?p IN (rdf:type))\n" + 
//				"    FILTER (?p = rdf:type)\n" + 
				"  }\n" + 
				"}");

		Query expected = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT * {\n" + 
				"     {}\n" + 
				"   UNION\n" + 
				"  { ?s rdf:type ?o\n" + 
				"    BIND(rdf:type AS ?p)\n" + 
				"  }\n" + 
				"}");
		
		
		Query actual = DataQueryImpl.rewrite(input, TransformRedundantFilterRemoval::transform);
		
		Assert.assertEquals("" + expected, "" + actual);
	}
	
	
	@Test
	public void testRewriteRedundantFilterAfterBindInUnion2() {
		Query input = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT * {\n" + 
				"     {}\n" + 
				"   UNION\n" + 
				"  { ?s rdf:type ?o\n" + 
				"    BIND(rdf:type AS ?p)\n" + 
				"    FILTER (?p IN (rdf:first, rdf:type))\n" + 
				"  }\n" + 
				"}");

		Query expected = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT * {\n" + 
				"     {}\n" + 
				"   UNION\n" + 
				"  { ?s rdf:type ?o\n" + 
				"    BIND(rdf:type AS ?p)\n" + 
				"  }\n" + 
				"}");
		
		Query actual = DataQueryImpl.rewrite(input, TransformRedundantFilterRemoval::transform);
		
		Assert.assertEquals("" + expected, "" + actual);
	}
	
	@Test
	public void testRewriteRedundantFilterAfterBindInUnion3() {
		Query input = QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT * {\n" + 
				"     {}\n" + 
				"   UNION\n" + 
				"  { ?s rdf:type ?o\n" + 
				"    BIND(rdf:type AS ?p)\n" + 
				"    FILTER (?p IN (rdf:first))\n" + 
				"  }\n" + 
				"}");

		Query expected = DataQueryImpl.rewrite(QueryFactory.create("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"SELECT * {\n" + 
				"     {}\n" + 
				"   UNION\n" + 
				"  { ?s rdf:type ?o\n" + 
				"    BIND(rdf:type AS ?p)\n" +
				"    FILTER ( false )\n" + 
				"  }\n" + 
				"}"), Function.identity());
		
		Query actual = DataQueryImpl.rewrite(input, TransformRedundantFilterRemoval::transform);
		
		Assert.assertEquals("" + expected, "" + actual);
	}
}
