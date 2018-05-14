package org.hobbit.benchmark.faceted_browsing.v2;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.junit.Test;

public class TestFacetedBrowsingQuery {
	@Test
	public void testFactedBrowsingQuery() {
		Dataset ds = DatasetFactory.create();
		try(RDFConnection conn = RDFConnectionFactory.connect(ds)) {
			//conn.update("CREATE SILENT GRAPH <http://example.org>");
			//String graph = "http://example.org";
			conn.load("podigg-lc-small.ttl");
	       try(QueryExecution xxx = conn.query("SELECT ?g ?s ?p ?o { GRAPH ?g { ?s ?p ?o } } ORDER BY ?g ?s ?p ?o LIMIT 10")) {
	    	   System.out.println(ResultSetFormatter.asText(xxx.execSelect()));
	       }

			String q = "PREFIX  eg:   <http://www.example.org/>\n" + 
					"PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" + 
					"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" + 
					"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
					"PREFIX  gtfs: <http://vocab.gtfs.org/terms#>\n" + 
					"PREFIX  vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>\n" + 
					"PREFIX  lcd:  <http://semweb.mmlab.be/ns/linked-connections-delay#>\n" + 
					"PREFIX  td:   <http://purl.org/td/transportdisruption#>\n" + 
					"PREFIX  rss:  <http://purl.org/rss/1.0/>\n" + 
					"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
					"PREFIX  ja:   <http://jena.hpl.hp.com/2005/11/Assembler#>\n" + 
					"PREFIX  lc:   <http://semweb.mmlab.be/ns/linkedconnections#>\n" + 
					"PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" + 
					"\n" + 
					"SELECT DISTINCT  ?connection\n" +
					"FROM NAMED <http://example.org>\n" +
					"WHERE\n" + 
					"  { ?connection  lc:departureStop   ?stop .\n" + 
					"    ?stop     <http://www.w3.org/2003/01/geo/wgs84_pos#lat>  ?lat ;\n" + 
					"              <http://www.w3.org/2003/01/geo/wgs84_pos#long>  ?long\n" + 
					"    FILTER ( ( ( ( 1.24875 < ?lat ) && ( ?lat < 8.74125 ) ) && ( 0.9984999999999999 < ?long ) ) && ( ?long < 9.1862 ) )\n" + 
					"  }\n" + 
					"";
			
			//q = OpAsQuery.asQuery(Algebra.unionDefaultGraph(Algebra.compile(QueryFactory.create(q)))).toString();
			//System.out.println(q);
			
			Query qq = QueryFactory.create(q, Syntax.syntaxARQ);
			try(QueryExecution qe = conn.query(qq)) {
				System.out.println(ResultSetFormatter.asText(qe.execSelect()));
			}
		}
	}
}
