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
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDataGenerator;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDataGeneratorFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceFactory;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceManagerClient;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceManagerServer;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigEvaluationModule;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigEvaluationStorage;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigEvaluationStorageStorageProvider;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigRabbitMqConnection;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigSystemAdapter;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigTaskGenerator;
import org.hobbit.benchmark.faceted_browsing.config.ConfigsFacetedBrowsingBenchmark.ConfigTaskGeneratorFacetedBenchmark;
import org.hobbit.benchmark.faceted_browsing.main.LauncherServiceCapable;
import org.hobbit.core.component.BenchmarkControllerFacetedBrowsing;
import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmarkMocha;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.service.api.DockerServiceDelegateWrapper;
import org.hobbit.core.service.api.ServiceDelegate;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.rdf.component.SystemAdapterRDFConnectionMocha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.google.common.collect.ImmutableMap;

public class ConfigVirtualDockerServiceFactory {
	private static final Logger logger = LoggerFactory.getLogger(ConfigVirtualDockerServiceFactory.class);


	public static Map<String, Supplier<SpringApplicationBuilder>> getVirtualDockerComponentRegistry() {

		Function<String, SpringApplicationBuilder> createComponentBaseConfig = componentName -> new SpringApplicationBuilder()
				.properties(new ImmutableMap.Builder<String, Object>()
						.put("componentName", componentName)
						.build())
				.sources(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class)
					.child(ConfigDockerServiceManagerClient.class);

		// Note: We make the actual components children of the channel configuration, so that we ensure that
		// channels are only closed once the components have shut down and sent their final messages
		Supplier<SpringApplicationBuilder> bcAppBuilder = () -> createComponentBaseConfig.apply("bc")
				.child(ConfigBenchmarkControllerFacetedBrowsingServices.class)
					.child(BenchmarkControllerFacetedBrowsing.class, LauncherServiceCapable.class);
		
		Supplier<SpringApplicationBuilder> dgAppBuilder = () -> createComponentBaseConfig.apply("dg")
				.child(ConfigDataGeneratorFacetedBrowsing.class, ConfigDataGenerator.class)
						.child(DataGeneratorFacetedBrowsing.class, LauncherServiceCapable.class);
		
		Supplier<SpringApplicationBuilder> tgAppBuilder = () -> createComponentBaseConfig.apply("tg")
				.child(ConfigEncodersFacetedBrowsing.class, ConfigTaskGenerator.class, ConfigTaskGeneratorFacetedBenchmark.class)
					.child(TaskGeneratorFacetedBenchmarkMocha.class, LauncherServiceCapable.class);

		Supplier<SpringApplicationBuilder> saAppBuilder = () -> createComponentBaseConfig.apply("sa")
				.child(ConfigEncodersFacetedBrowsing.class, ConfigSystemAdapter.class)
					.child(SystemAdapterRDFConnectionMocha.class, LauncherServiceCapable.class);
			
		Supplier<SpringApplicationBuilder> esAppBuilder = () -> createComponentBaseConfig.apply("es")
				.child(ConfigEvaluationStorage.class, ConfigEvaluationStorageStorageProvider.class)
					.child(DefaultEvaluationStorage.class, LauncherServiceCapable.class);		
		
		Supplier<SpringApplicationBuilder> emAppBuilder = () -> createComponentBaseConfig.apply("em")
				.child(ConfigEvaluationModule.class)
					.child(EvaluationModuleComponent.class, LauncherServiceCapable.class);
		
		
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
        result.put("git.project-hobbit.eu:4567/gkatsibras/defaultevaluationstorage/image", esAppBuilder);
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

	

	public static DockerServiceFactory<?> applyServiceWrappers(DockerServiceFactory<?> delegate) {

        // Service wrappers which modifies startup/shutdown of other services; mostly healthchecks
        // on startup
        Map<Pattern, Function<DockerService, DockerService>> serviceWrappers = new LinkedHashMap<>();
        serviceWrappers.put(Pattern.compile("tenforce/virtuoso"), dockerService -> {
        	DockerService r = new DockerServiceDelegateWrapper<DockerService>(dockerService) {
        		// FIXME We want to enhance the startup method within the thread allocated by the guava service
        		@Override
        		public ServiceDelegate<DockerService> startAsync() {
        			super.startAsync().awaitRunning();
        			// The delegate has started, so we have a container id
        			String host = delegate.getContainerId();
    	        	String destination = "http://" + host + ":8890/";
        			
    	        	new HealthcheckRunner(
    	        			60, 1, TimeUnit.SECONDS, () -> {
	        		        try (RDFConnection conn = RDFConnectionFactory.connect(destination)) {
	        		            conn.querySelect("SELECT * { <http://example.org/healthcheck> a ?t }", qs -> {});
	        		        }
    	        	}).run();
    	        	return this;
        		}
        	};
        	
        	return r;
        	//new org.hobbit.benchmark.faceted_browsing.config.ServiceDelegate<>(delegate);
        });

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