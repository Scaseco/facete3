package org.hobbit.benchmark.faceted_browsing.v1.config;

import org.hobbit.benchmark.faceted_browsing.v1.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.component.EvaluationModule;
import org.springframework.context.annotation.Bean;

public class ConfigEvaluationModuleFacetedBrowsing {
    @Bean
    public EvaluationModule evaluationModule() {
    	return new EvaluationModuleFacetedBrowsingBenchmark();
    }
}
