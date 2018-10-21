package org.hobbit.benchmark.faceted_browsing.v2.config;

import org.hobbit.benchmark.faceted_browsing.v2.impl.EvaluationModuleRdfGeneric;
import org.hobbit.core.component.EvaluationModule;
import org.springframework.context.annotation.Bean;

public class ConfigEvaluationModuleFacetedBrowsing {
    @Bean
    public EvaluationModule evaluationModule() {
    	return new EvaluationModuleRdfGeneric();
    	//return new EvaluationModuleFacetedBrowsingBenchmark();
    }
}
