package org.hobbit.benchmark.faceted_browsing.v2.engine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.lookup.CountInfo;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdfconnection.RDFConnection;

import com.google.common.collect.Range;

public class LookupServicePreCount
	implements LookupService<Node, Range<Long>>
{
    protected RDFConnection conn;
    protected Map<Property, BinaryRelation> facetRelationIndex;
    protected long rowLimit = 10000;
    
	public LookupServicePreCount(RDFConnection conn, Map<Node, BinaryRelation> facetRelationIndex, long rowLimit) {
		super();
		this.conn = conn;
		this.facetRelationIndex = facetRelationIndex;
		this.rowLimit = rowLimit;
	}


	@Override
	public CompletableFuture<Map<Node, CountInfo>> apply(Iterable<K> t) {
//        var rowLimit = this.rowLimit;
//
//        var countVar = VarUtils.c;
//
//        // Perform lookup with rowLimit + 1
//        var subQueries = CountUtils.createQueriesPreCount(this.facetRelationIndex, countVar, properties, rowLimit + 1);
//        var exec = CountUtils.execQueries(this.sparqlService, subQueries, this.facetRelationIndex.getSourceVar(), countVar);
//
//        var result = exec.then(function(map) {
//            var r = new HashMap();
//
//            var entries = map.entries();
//            entries.forEach(function(entry) {
//                var property = entry.key;
//                var count = entry.val;
//
//                var hasMoreItems = count > rowLimit;
//                var countInfo = {
//                    count: hasMoreItems ? rowLimit : count,
//                    hasMoreItems: hasMoreItems
//                };
//
//                r.put(property, countInfo);
//            });
//
//            return r;
//        });
//
//        return result;
//		
//		
//		// TODO Auto-generated method stub
//		return null;
	}


	@Override
	public CompletableFuture<Map<Node, CountInfo>> apply(Iterable<Node> t) {
		// TODO Auto-generated method stub
		return null;
	}

}
