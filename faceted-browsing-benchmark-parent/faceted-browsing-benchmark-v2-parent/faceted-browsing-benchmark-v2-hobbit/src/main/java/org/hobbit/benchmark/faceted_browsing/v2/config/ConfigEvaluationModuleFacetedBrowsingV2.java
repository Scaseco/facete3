package org.hobbit.benchmark.faceted_browsing.v2.config;

import org.hobbit.benchmark.faceted_browsing.component.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.component.EvaluationModule;
import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;

public class ConfigEvaluationModuleFacetedBrowsingV2 {
    @Bean
    public EvaluationModule evaluationModule(Gson gson) {
    	//return new EvaluationModuleRdfGeneric();
    	return new EvaluationModuleFacetedBrowsingBenchmark(buffer -> FacetedBrowsingEncodersV2.decodeExpectedDataV2(buffer, gson));
    }
}
