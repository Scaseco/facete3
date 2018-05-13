package org.hobbit.benchmark.faceted_browsing.config;

import java.io.ByteArrayInputStream;

import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.ResourceUtils;
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

//	@Bean
//	@Autowired
//	public AbstractEvaluationStorage mockEs(Environment env) throws Exception {
//		
//		
//	    Map<String, String> map = new HashMap<>(System.getenv());
//	    map.put(Constants.HOBBIT_SESSION_ID_KEY, env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY));
//	    map.put(Constants.RABBIT_MQ_HOST_NAME_KEY, env.getProperty(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost"));
//	    map.put(Constants.ACKNOWLEDGEMENT_FLAG_KEY, "true");
//	    ConfigsFacetedBrowsingBenchmark.BenchmarkLauncher.setEnv(map);
//	    
//	    InMemoryEvaluationStore es = new InMemoryEvaluationStore() {
//			@Override
//			public void receiveExpectedResponseData(String taskId, long timestamp, byte[] data) {
//				System.out.println("Got expected result for " + taskId);
//				super.receiveExpectedResponseData(taskId, timestamp, data);
//			}
//			
//			@Override
//			public void receiveResponseData(String taskId, long timestamp, byte[] data) {
//				System.out.println("Got actual result for " + taskId);
//				super.receiveResponseData(taskId, timestamp, data);
//			}
//		};
//
//        return es;
//	}

    @Bean
    public Resource experimentResult(
    	@Value("${" + Constants.HOBBIT_EXPERIMENT_URI_KEY + ":" + Constants.NEW_EXPERIMENT_URI + "}") String experimentUri,
    	@Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
    	
    	
    	// Deserialize the paramModel, rename them to the given experiment URI, and yield the
    	// resource of the experiment
        Model paramModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(paramModel, new ByteArrayInputStream(paramModelStr.getBytes()), Lang.JSONLD);

        Resource tmp = paramModel.createResource(Constants.NEW_EXPERIMENT_URI);
        
        Resource result = ResourceUtils.renameResource(tmp, experimentUri);

        return result;
    }
    
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
	        		.setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage/image")
//	                .setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0")
//	        		.setImageName("git.project-hobbit.eu:4567/cstadler/evaluationstorage/image")
	                .setLocalEnvironment(ImmutableMap.<String, String>builder()
	                        .put(Constants.ACKNOWLEDGEMENT_FLAG_KEY, "true")
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
