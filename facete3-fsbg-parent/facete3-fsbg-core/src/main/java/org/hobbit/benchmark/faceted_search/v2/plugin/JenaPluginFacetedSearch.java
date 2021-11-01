package org.hobbit.benchmark.faceted_search.v2.plugin;

import org.aksw.jenax.arq.util.implementation.SimpleImplementation;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.hobbit.benchmark.faceted_browsing.v2.main.SparqlTaskResource;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa.ScenarioConfig;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RdfStack;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.RdfStackImpl;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummary;
import org.hobbit.benchmark.faceted_browsing.v2.vocab.SetSummaryImpl;

public class JenaPluginFacetedSearch implements JenaSubsystemLifecycle {
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

//	@Override
//	public int level() {
//		return ;
//	}

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        p.add(SetSummary.class, new SimpleImplementation(SetSummaryImpl::new));
        p.add(RdfStack.class, new SimpleImplementation(RdfStackImpl::new));
        JenaPluginUtils.scan(SparqlTaskResource.class.getPackage().getName());
        JenaPluginUtils.registerResourceClasses(ScenarioConfig.class);
    }
}