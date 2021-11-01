package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.BenchmarkVocab;


public interface SparqlTaskResource
	extends Resource
{
	//@Iri("rdfs:label")
	@Iri(BenchmarkVocab.Strs.taskPayload)
	String getSparqlStmtString();
	SparqlTaskResource setSparqlStmtString(String stmtStr);
	
	// Parse the resource's data
//	default SparqlStmt parseAsSparqlStmt() {
//		String str = getSparqlStmtString();
//		SparqlStmtParserImpl parser = SparqlStmtParserImpl.create(Syntax.syntaxSPARQL_11, false);
//		SparqlStmt result = parser.apply(str);
//		return result;
//	}
	
	public static SparqlStmt parse(SparqlTaskResource r) {
		String str = r.getSparqlStmtString();
		SparqlStmtParserImpl parser = SparqlStmtParserImpl.create(Syntax.syntaxSPARQL_11, false);
		SparqlStmt result = parser.apply(str);
		return result;
		
	}
}