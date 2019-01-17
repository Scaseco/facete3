package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;

/**
 * 
 * Wrapper for a CONSTRUCT query with one designated 'root' variable whose template forms a tree rooted in that variable
 * 
 * A rooted query has a defined schema, which enables clients to check which attributes are being retrieved
 * TODO Again: Shouldn't this be possible with Shacl?
 * 
 * @author Claus Stadler, Jul 21, 2018
 *
 */
public class RootedQuery {
	protected Var rootVar;
	protected Query query;
	
//	public getPath() {
//		
//	}
}
