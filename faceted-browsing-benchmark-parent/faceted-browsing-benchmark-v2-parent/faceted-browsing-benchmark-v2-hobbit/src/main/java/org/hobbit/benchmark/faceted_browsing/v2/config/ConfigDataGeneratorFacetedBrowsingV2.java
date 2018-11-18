package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.Constants;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


public class ConfigDataGeneratorFacetedBrowsingV2 {
    @Bean
    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
    	return () -> Stream.of(new Triple(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property));
    }
}
