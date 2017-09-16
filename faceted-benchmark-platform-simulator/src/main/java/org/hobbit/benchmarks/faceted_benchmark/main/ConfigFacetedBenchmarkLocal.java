package org.hobbit.benchmarks.faceted_benchmark.main;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigFacetedBenchmarkLocal {
    @Bean
    public SparqlServiceSupplier sparqlServiceSupplier() {
        return new SparqlServiceSupplierDefault();
    }
}
