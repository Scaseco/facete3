package org.hobbit.benchmark.faceted_browsing.v1.config;

import java.util.Collections;
import java.util.Map.Entry;

import org.hobbit.benchmark.faceted_browsing.component.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.v1.evaluation.ChokePoints;
import org.hobbit.core.component.EvaluationModule;
import org.springframework.context.annotation.Bean;

public class ConfigEvaluationModuleFacetedBrowsingV1 {
    @Bean
    public EvaluationModule evaluationModule() {
    	return new EvaluationModuleFacetedBrowsingBenchmark(
    			id -> ChokePoints.getTable().entrySet().stream()
    				.filter(e -> e.getValue().contains(id))
    				.map(Entry::getKey)
    				.map(Collections::singleton)
    				.findFirst().orElse(null));
    }
}
