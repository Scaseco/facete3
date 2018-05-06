package org.hobbit.benchmark.faceted_browsing.config;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.core.Constants;
import org.hobbit.core.service.api.ServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.utils.CountingSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger logger = LoggerFactory.getLogger(ConfigBenchmarkControllerFacetedBrowsingServices.class);

    
    // It is valid to have dependencies in a configuration class:
    // Section "Working with externalized values" in
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html
    
    
    @Autowired
    protected DockerServiceBuilderFactory<?> dockerServiceBuilderFactory;

    @Bean
    public ServiceBuilder<?> dataGeneratorServiceFactory(@Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModel) {
        
        logger.info("BC: Configuring DG with parameter model: " + paramModel);
                
        return CountingSupplier.from(count ->
        	dockerServiceBuilderFactory.get()
                .setImageName("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image")
                .setLocalEnvironment(ImmutableMap.<String, String>builder()
                        .put("NODE_MEM", "1000")
                        .put(Constants.BENCHMARK_PARAMETERS_MODEL_KEY, paramModel)
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
//	        		.setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage/image")
//	                .setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0")
	        		.setImageName("git.project-hobbit.eu:4567/cstadler/evaluationstorage/image")
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
