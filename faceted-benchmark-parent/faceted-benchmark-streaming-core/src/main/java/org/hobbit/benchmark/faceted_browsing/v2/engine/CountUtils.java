package org.hobbit.benchmark.faceted_browsing.v2.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;


//interface Relation2 {
//	joinOn();
//	filter(Expr)
//
//	getRoot();
//}

public class CountUtils {
		Map<Node, BinaryRelation> createQueriesPreCountCore(
    		Map<Node, BinaryRelation> overrides,
    		TernaryRelation defaultRelation, // TODO Maybe use a lambda Function<Node, BinaryRelation> instead
    		Var countVar,
    		Collection<Node> properties) {
    	    	
    	
    	Map<Node, BinaryRelation> result = new HashMap<>();
    	
    	// Use the default relation for every property that is not in the overrides map
    	for(Node p : properties) {
    		BinaryRelation r = overrides.get(p);
    		
    		if(r == null) {
    			TernaryRelation tr = defaultRelation.filterP(p);
    			BinaryRelation br = new BinaryRelation(tr.getElement(), tr.getS(), tr.getO());
    			result.put(p, br);
    		} else {
    			result.put(p, r);
    		}
    	}
    	
    	
    	return result;
    }

	public static Query createQueryCount(Map<Node, BinaryRelation> components, Long subLimit) {
		return null;
		
		
	}
		
//createQueriesPreCount: function(facetRelationIndex, countVar, properties, rowLimit) {
//    var result = this.createQueriesPreCountCore(facetRelationIndex, countVar, properties, function(relation, property, countVar) {
//        var r = RelationUtils.createQueryRawSize(relation, property, countVar, rowLimit);
//        return r;
//    });
//
//    return result;
//},
}
