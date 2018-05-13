package org.hobbit.benchmark.faceted_browsing.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.ext.virtuoso.HealthcheckRunner;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigCommandChannel;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigCommunicationWrapper;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDataGenerator;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDataGeneratorFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDataQueueFactory;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceFactory;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceManagerClient;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceManagerServer;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigEvaluationModule;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigEvaluationStorage;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigEvaluationStorageStorageProvider;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigQueueNameMapper;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigRabbitMqConnection;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigSystemAdapter;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigTaskGenerator;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigTaskGeneratorFacetedBenchmark;
import org.hobbit.core.Constants;
import org.hobbit.core.component.BenchmarkControllerFacetedBrowsing;
import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmarkMocha;
import org.hobbit.core.components.test.InMemoryEvaluationStore;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.service.api.DockerServiceDelegate;
import org.hobbit.core.service.api.ServiceDelegate;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.rdf.component.SystemAdapterRDFConnectionMocha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

public class ConfigVirtualDockerServiceFactory {
	private static final Logger logger = LoggerFactory.getLogger(ConfigVirtualDockerServiceFactory.class);

	public static final String COMPONENT_NAME_KEY = "componentName";
    public static final String DEFAULT_REQUESTED_CONTAINER_TYPE_KEY = "defaultRequstedContainerType";
	
	
	public static SpringApplicationBuilder createComponentBaseConfig(String componentName, String defaultRequestedContainerType) {
	    SpringApplicationBuilder result = new SpringApplicationBuilder()
	            .properties(new ImmutableMap.Builder<String, Object>()
	                    .put(COMPONENT_NAME_KEY, componentName)
	                    .put(DEFAULT_REQUESTED_CONTAINER_TYPE_KEY, defaultRequestedContainerType)
	                    .build())
	            .sources(ConfigGson.class, ConfigCommunicationWrapper.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigQueueNameMapper.class, ConfigDataQueueFactory.class)
	                .child(ConfigDockerServiceManagerClient.class);
	    return result;
	}


	public static Map<String, Supplier<SpringApplicationBuilder>> getVirtualDockerComponentRegistry() {

		//Function<String, SpringApplicationBuilder> baseConfigFactory = ConfigVirtualDockerServiceFactory::createComponentBaseConfig;

		// Note: We make the actual components children of the channel configuration, so that we ensure that
		// channels are only closed once the components have shut down and sent their final messages
		Supplier<SpringApplicationBuilder> bcAppBuilder = () -> createComponentBaseConfig("bc", Constants.CONTAINER_TYPE_BENCHMARK)
				.child(ConfigBenchmarkControllerFacetedBrowsingServices.class)
					.child(BenchmarkControllerFacetedBrowsing.class);
		
		Supplier<SpringApplicationBuilder> dgAppBuilder = () -> createComponentBaseConfig("dg", Constants.CONTAINER_TYPE_BENCHMARK)
				.child(ConfigDataGeneratorFacetedBrowsing.class, ConfigDataGenerator.class)
						.child(DataGeneratorFacetedBrowsing.class);
		
		Supplier<SpringApplicationBuilder> tgAppBuilder = () -> createComponentBaseConfig("tg", Constants.CONTAINER_TYPE_BENCHMARK)
				.child(ConfigEncodersFacetedBrowsing.class, ConfigTaskGenerator.class, ConfigTaskGeneratorFacetedBenchmark.class)
					.child(TaskGeneratorFacetedBenchmarkMocha.class);

		Supplier<SpringApplicationBuilder> saAppBuilder = () -> createComponentBaseConfig("sa", Constants.CONTAINER_TYPE_SYSTEM)
				.child(ConfigEncodersFacetedBrowsing.class, ConfigSystemAdapter.class)
					.child(SystemAdapterRDFConnectionMocha.class);
			
		Supplier<SpringApplicationBuilder> esAppBuilder = () -> createComponentBaseConfig("es", Constants.CONTAINER_TYPE_DATABASE)
				.child(ConfigEncodersFacetedBrowsing.class, ConfigEvaluationStorage.class, ConfigEvaluationStorageStorageProvider.class)
					.child(DefaultEvaluationStorage.class);
		
		Supplier<SpringApplicationBuilder> emAppBuilder = () -> createComponentBaseConfig("em", Constants.CONTAINER_TYPE_SYSTEM)
				.child(ConfigEncodersFacetedBrowsing.class, ConfigEvaluationModule.class)
					.child(EvaluationModuleComponent.class);
		
		
		Supplier<SpringApplicationBuilder> qpidServerAppBuilder = () -> new SpringApplicationBuilder()
				.sources(ConfigQpidBroker.class);
		
		Supplier<SpringApplicationBuilder> dockerServiceManagerServerAppBuilder = () -> new SpringApplicationBuilder()
				.sources(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigDockerServiceFactory.class)
					.child(ConfigDockerServiceManagerServer.class);

		
		Map<String, Supplier<SpringApplicationBuilder>> result = new LinkedHashMap<>();
        result.put("git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image", bcAppBuilder);
		
        result.put("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image", dgAppBuilder);
        result.put("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image", tgAppBuilder);        
//        result.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0", esAppBuilder);
        result.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage/image", esAppBuilder);
        //result.put("git.project-hobbit.eu:4567/cstadler/evaluationstorage/image", esAppBuilder);
        result.put("git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image", emAppBuilder);

        // NOTE The sa is started by the platform
        result.put("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image", saAppBuilder);		
		

		result.put("git.project-hobbit.eu:4567/gkatsibras/qpidserver/image", qpidServerAppBuilder);

		result.put("git.project-hobbit.eu:4567/gkatsibras/dockerservicemanagerserver/image", dockerServiceManagerServerAppBuilder);
		
		return result;
	}
	
	public static DockerServiceFactory<?> createVirtualComponentDockerServiceFactory() {

		Map<String, Supplier<SpringApplicationBuilder>> virtualDockerComponentRegistry = getVirtualDockerComponentRegistry();
        DockerServiceFactory<?> result = new DockerServiceFactorySpringApplicationBuilder(virtualDockerComponentRegistry);

		return result;
	}

	public static DockerService wrapSparqlServiceWithHealthCheck(DockerService dockerService, Integer port) {
    	DockerService result = new DockerServiceDelegate<DockerService>(dockerService) {
    		// FIXME We want to enhance the startup method within the thread allocated by the guava service
    		@Override
    		public ServiceDelegate<DockerService> startAsync() {
    			super.startAsync().awaitRunning();
    			// The delegate has started, so we have a container id
    			String host = delegate.getContainerId();
	        	String destination = "http://" + host + (port == null ? "" : ":" + port) + "/";
    			
	        	new HealthcheckRunner(
	        			60, 1, TimeUnit.SECONDS, () -> {
        		        try (RDFConnection conn = RDFConnectionFactory.connect(destination)) {
        		            conn.querySelect("SELECT * { <http://example.org/healthcheck> a ?t }", qs -> {});
        		        }
	        	}).run();
	        	return this;
    		}
    	};
    	
    	return result;
	}
	

	public static DockerServiceFactory<?> applyServiceWrappers(DockerServiceFactory<?> delegate) {

        // Service wrappers which modifies startup/shutdown of other services; mostly healthchecks
        // on startup
        Map<Pattern, Function<DockerService, DockerService>> serviceWrappers = new LinkedHashMap<>();
        serviceWrappers.put(Pattern.compile("tenforce/virtuoso"), dockerService -> wrapSparqlServiceWithHealthCheck(dockerService, 8890));

        DockerServiceFactory<?> result = (imageName, env) -> {

        	DockerService r = delegate.create(imageName, env);

        	Map<Pattern, Function<DockerService, DockerService>> cands =
        			serviceWrappers.entrySet().stream()
        			.filter(x -> x.getKey().matcher(imageName).find())
        			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));


        	for(Entry<Pattern, Function<DockerService, DockerService>> cand : cands.entrySet()) {	        		
        		logger.info("Applying service decorator: " + cand.getKey() + " to docker image " + imageName);
        		r = cand.getValue().apply(r);
        	}

        	return r;	        	
        };

        return result;
	}
			
}