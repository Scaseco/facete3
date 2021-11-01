package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jenax.stmt.core.SparqlStmt;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Main class for generating a stream of SPARQL task resources.
 * @author Claus Stadler, Oct 19, 2018
 *
 */
public class FacetedBrowsingV2TaskGenerator {
    //@javax.annotation.Resource
    //protected BiFunction<? super SparqlQueryConnection, ? super SparqlQueryConnection, ? extends Flowable<? extends Resource>> taskSupplierFactory;

    
//	public Flowable<SparqlTaskResource> generateTasks() {
//		
//	}
	public static void main(String[] args) {

		SparqlTaskResource r = ModelFactory.createDefaultModel().createResource().as(SparqlTaskResource.class);
		r.setSparqlStmtString("SELECT * { ?s ?p ?o }");
		SparqlStmt stmt = SparqlTaskResource.parse(r);
		System.out.println(stmt.getAsQueryStmt());
		
		// Test whether default methods work with our proxy util
	}
}
