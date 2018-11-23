package org.hobbit.sdk.sys;

import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfigImpl;

public class JenaPluginHobbitSdk
	implements JenaSubsystemLifecycle
{
	public void start() {
		init();
	}

	@Override
	public void stop() {
	}
	
	public static void init() {
		init(BuiltinPersonalities.model);		
	}
	
	public static void init(Personality<RDFNode> p) {
    	p.add(BenchmarkConfig.class, new SimpleImplementation(BenchmarkConfigImpl::new));
    }
}
