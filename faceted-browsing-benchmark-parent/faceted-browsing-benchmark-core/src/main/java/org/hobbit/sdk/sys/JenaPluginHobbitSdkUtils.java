package org.hobbit.sdk.sys;

import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfigImpl;

public class JenaPluginHobbitSdkUtils {
	public static void init() {
		init(BuiltinPersonalities.model);		
	}
	
	public static void init(Personality<RDFNode> p) {
    	p.add(BenchmarkConfig.class, new SimpleImplementation(BenchmarkConfigImpl::new));
    }
}
