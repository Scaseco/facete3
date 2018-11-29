package org.hobbit.benchmark.faceted_browsing.v1.config;

import org.hobbit.benchmark.faceted_browsing.component.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.component.EvaluationModule;
import org.springframework.context.annotation.Bean;

public class ConfigEvaluationModuleFacetedBrowsingV1 {
    @Bean
    public EvaluationModule evaluationModule() {
    	return new EvaluationModuleFacetedBrowsingBenchmark(FacetedBrowsingEncodersV1::decodeExpectedDataV1);
    }
}
