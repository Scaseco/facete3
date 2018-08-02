package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.List;
import java.util.stream.Stream;

import org.aksw.facete.v3.api.FacetNode;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;

public class TaskGenerator {

	protected RDFConnection conn;
	
	
	public TaskGenerator(RDFConnection conn) {
		this.conn = conn;
	}
	

	public Stream<Query> generate() {
		
		// Zoom into the map and find a region with amount of data in certain ranges
		
		
		
		
		
		return null;
	}
	
	
	public static void applyCp1(FacetNode fn) {
		System.out.println("Facets and counts: " + fn.fwd().facetCounts().exec().toList().blockingGet());
		
		
		//List<? extends RDFNode> available = fn.availableValues().sample(true).limit(1).exec().toList().blockingGet();
		
		//System.out.println("CP1 Available: " + available);
		
		//RDFNode value = Iterables.getFirst(available, null);
//		if(value != null) {
//			//fn.fwd
//		}
		
		//fq.root().out(property).constraints().eq(value).end().availableValues().exec()	
	}
	
	
	public void simulateNavigation() {
		// perform an entity type switch
		// -> 
		
		
		
		
		
	}
	
}
