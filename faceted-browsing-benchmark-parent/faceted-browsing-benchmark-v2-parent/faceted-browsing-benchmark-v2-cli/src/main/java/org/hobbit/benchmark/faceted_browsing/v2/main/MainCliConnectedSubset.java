package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.riot.RDFDataMgr;

public class MainCliConnectedSubset {
	public static void main(String[] args) {
		JenaPluginUtils.registerJenaResourceClasses(PathNode.class);

		PathNode test = RDFDataMgr.loadModel("skyscraper-paths.ttl").listResourcesWithProperty(PathNode.DEPTH, 0).nextResource().as(PathNode.class);
		
		System.out.println(test.getPredicate());
		System.out.println(test.getCount());
		System.out.println(test.getTransitions());
		
		
		for(PathNode pn : test.getTransitions().values()) {
			System.out.println(pn.getPredicate() + " " + pn.getCount() + " " + pn.getTotalCount());
		}
		
		int desiredTripleCount = 1000;
		int desiredConnectionsBetweenLevels = 3;
		int maxLevels = 3;
		int predicatesPerTarget = 7;

		
	}
}

