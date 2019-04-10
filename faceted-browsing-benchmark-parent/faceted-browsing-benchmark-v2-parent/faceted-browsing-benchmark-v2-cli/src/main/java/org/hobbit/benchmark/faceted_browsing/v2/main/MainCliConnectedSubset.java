package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.riot.RDFDataMgr;

public class MainCliConnectedSubset {
	public static void main(String[] args) {
		JenaPluginUtils.registerJenaResourceClasses(PathNode.class);

		PathNode test = RDFDataMgr.loadModel("path-node.ttl").createResource("http://www.example.org/foo").as(PathNode.class);
		
		System.out.println(test.getPredicate());
		System.out.println(test.getCount());
		System.out.println(test.getTransitions());
	}
}
