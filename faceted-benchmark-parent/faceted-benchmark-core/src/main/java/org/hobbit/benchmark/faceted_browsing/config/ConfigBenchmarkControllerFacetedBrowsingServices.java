package org.hobbit.benchmark.faceted_browsing.config;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.hobbit.core.service.api.ServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.utils.CountingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * This configuration is intended to be loaded programmatically
 * by the Bootstrapping*Controller implementations, because
 * it needs the services provided it
 *
 *
 * @author raven Sep 21, 2017
 *
 */
@Configuration
public class ConfigBenchmarkControllerFacetedBrowsingServices {
    // It is valid to have dependencies in a configuration class:
    // Section "Working with externalized values" in
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html

    @Autowired
    protected DockerServiceBuilderFactory<?> dockerServiceBuilderFactory;



    @Bean
    public ServiceBuilder<?> dataGeneratorServiceFactory() {
        return CountingSupplier.from(count ->
        	dockerServiceBuilderFactory.get()
                .setImageName("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image")
                .setLocalEnvironment(ImmutableMap.<String, String>builder()
                        .put("NODE_MEM", "1000")
                        .build())
                ).get();
    }

    @Bean
    public ServiceBuilder<?> taskGeneratorServiceFactory() {
        return CountingSupplier.from(count ->
	    	dockerServiceBuilderFactory.get()
	            .setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image")
	            .setLocalEnvironment(ImmutableMap.<String, String>builder()
	                    .build())
	            ).get();
    }

    @Bean
    public ServiceBuilder<?> evaluationStorageServiceFactory() {
        return CountingSupplier.from(count ->
	        dockerServiceBuilderFactory.get()
	        		.setImageName("git.project-hobbit.eu:4567/gkatsibras/defaultevaluationstorage/image")
//	                .setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0")
	                .setLocalEnvironment(ImmutableMap.<String, String>builder()
	                        .put("ACKNOWLEDGEMENT_FLAG", "true")
	                        .build())
	                ).get();
    }

    @Bean
    public ServiceBuilder<?> evaluationModuleServiceFactory() {
        return CountingSupplier.from(count ->
	        dockerServiceBuilderFactory.get()
	                .setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image")
	                .setLocalEnvironment(ImmutableMap.<String, String>builder()
	                        .build())
	                ).get();
    }

}
