package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.stream.Stream;

import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskGenerator {

	private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);
	
	protected RDFConnection conn;
	
	
	public TaskGenerator(RDFConnection conn) {
		this.conn = conn;
	}
	

	public Stream<Query> generate() {
		
		// Zoom into the map and find a region with amount of data in certain ranges
		
		
		
		
		
		return null;
	}
	

	/**
	 * Cp1: Select a facet + value and add it as constraint
	 */
	public static void applyCp1(FacetNode fn) {
		FacetValueCount fc = fn.fwd().facetValueCounts().sample(true).limit(1).exec().firstElement().blockingGet();
		if(fc != null) {
			fn.fwd(fc.getPredicate()).one().constraints().eq(fc.getValue());
			
			// Pick one of the facet values
			logger.info("Applying cp1: " + fn.root().availableValues().exec().toList().blockingGet());
		}
	}
	
	/**
	 * Find all instances which additionally realize this property path with any property value
	 */
	public static void applyCp2(FacetNode fn) {
		Node node = fn.fwd().facets().sample(true).limit(1).exec().firstElement().map(RDFNode::asNode).blockingGet();
		if(node != null) {
			fn.fwd(node).one().constraints().exists();
			
			// Pick one of the facet values
			logger.info("Applying cp1) " + fn.root().availableValues().exec().toList().blockingGet());
		}
	}
		
		
		
//		System.out.println("Facets and counts: " + fn.fwd().facetValueCounts().exec().toList().blockingGet());


		//List<? extends RDFNode> available = fn.availableValues().sample(true).limit(1).exec().toList().blockingGet();
		
		//System.out.println("CP1 Available: " + available);
		
		//RDFNode value = Iterables.getFirst(available, null);
//		if(value != null) {
//			//fn.fwd
//		}
		
		//fq.root().out(property).constraints().eq(value).end().availableValues().exec()	
	
	
	public void simulateNavigation() {
		// perform an entity type switch
		// -> 
		
		
		
		
		
	}
	
}
