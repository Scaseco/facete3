package org.hobbit.benchmark.faceted_browsing.v2.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hobbit.benchmark.faceted_browsing.component.ConfigSystemAdapter;
import org.hobbit.benchmark.faceted_browsing.config.BenchmarkConfig;
import org.hobbit.benchmark.faceted_browsing.config.ComponentUtils;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceFactory;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceManagerServer;
import org.hobbit.benchmark.faceted_browsing.config.ConfigEvaluationModule;
import org.hobbit.benchmark.faceted_browsing.config.ConfigEvaluationStorage;
import org.hobbit.benchmark.faceted_browsing.config.ConfigEvaluationStorageStorageProvider;
import org.hobbit.benchmark.faceted_browsing.config.ConfigTaskGenerator;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigCommandChannel;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigDataGenerator;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigRabbitMqConnection;
import org.hobbit.core.Constants;
import org.hobbit.core.component.BenchmarkControllerComponentImpl;
import org.hobbit.core.component.DataGeneratorComponentImpl;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmarkMocha;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceFactorySpringApplicationBuilder;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.rdf.component.SystemAdapterRDFConnectionMocha;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

public class ConfigVirtualDockerServiceFactoryV2 {


	public static Map<String, Supplier<SpringApplicationBuilder>> getDockerServiceFactoryOverrides(BenchmarkConfig config) { 
		//Function<String, SpringApplicationBuilder> baseConfigFactory = ConfigVirtualDockerServiceFactory::createComponentBaseConfig;

		// Note: We make the actual components children of the channel configuration, so that we ensure that
		// channels are only closed once the components have shut down and sent their final messages
		Supplier<SpringApplicationBuilder> bcAppBuilder = () -> ComponentUtils.createComponentBaseConfig("bc", Constants.CONTAINER_TYPE_BENCHMARK)
				.child(ConfigBenchmarkControllerFacetedBrowsingServicesV2.class)
					.child(BenchmarkControllerComponentImpl.class);
		
		Supplier<SpringApplicationBuilder> dgAppBuilder = () -> ComponentUtils.createComponentBaseConfig("dg", Constants.CONTAINER_TYPE_BENCHMARK)
				.child(ConfigDataGeneratorFacetedBrowsingV2.class, ConfigDataGenerator.class)
						.child(DataGeneratorComponentImpl.class);
		
		Supplier<SpringApplicationBuilder> tgAppBuilder = () -> ComponentUtils.createComponentBaseConfig("tg", Constants.CONTAINER_TYPE_BENCHMARK)
				.child(ConfigEncodersFacetedBrowsingV2.class, ConfigTaskGenerator.class, ConfigTaskGeneratorFacetedBenchmarkV2.class)
					.child(TaskGeneratorFacetedBenchmarkMocha.class);

		Supplier<SpringApplicationBuilder> saAppBuilder = () -> ComponentUtils.createComponentBaseConfig("sa", Constants.CONTAINER_TYPE_SYSTEM)
				.child(ConfigEncodersFacetedBrowsingV2.class, ConfigSystemAdapter.class)
					.child(SystemAdapterRDFConnectionMocha.class);
			
		Supplier<SpringApplicationBuilder> esAppBuilder = () -> ComponentUtils.createComponentBaseConfig("es", Constants.CONTAINER_TYPE_DATABASE)
				.child(ConfigEncodersFacetedBrowsingV2.class, ConfigEvaluationStorage.class, ConfigEvaluationStorageStorageProvider.class)
					.child(DefaultEvaluationStorage.class);
		
		Supplier<SpringApplicationBuilder> emAppBuilder = () -> ComponentUtils.createComponentBaseConfig("em", Constants.CONTAINER_TYPE_SYSTEM)
				.child(ConfigEncodersFacetedBrowsingV2.class, ConfigEvaluationModule.class, ConfigEvaluationModuleFacetedBrowsingV2.class)
					.child(EvaluationModuleComponent.class);
		
		
		Supplier<SpringApplicationBuilder> qpidServerAppBuilder = () -> new SpringApplicationBuilder()
				.sources(ConfigQpidBroker.class);
		
		Supplier<SpringApplicationBuilder> dockerServiceManagerServerAppBuilder = () -> new SpringApplicationBuilder()
				.sources(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigDockerServiceFactory.class)
					.child(ConfigDockerServiceManagerServer.class);

		
		Map<String, Supplier<SpringApplicationBuilder>> map = new LinkedHashMap<>();
        
//		BenchmarkConfig config = FacetedBrowsingBenchmarkV1Constants.config;

		map.put(config.getBenchmarkControllerImageName(), bcAppBuilder);
		map.put(config.getDataGeneratorImageName(), dgAppBuilder);
		map.put(config.getTaskGeneratorImageName(), tgAppBuilder);
		map.put(config.getEvaluationModuleImageName(), emAppBuilder);
		
//		{
//			String prefixV1 = "git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/faceted-browsing-benchmark-v1-";
//	
//			map.put(prefixV1 + "benchmark-controller", bcAppBuilder);		
//	        map.put(prefixV1 + "data-generator", dgAppBuilder);
//	        map.put(prefixV1 + "task-generator", tgAppBuilder);
//	        map.put(prefixV1 + "evaluation-module", emAppBuilder);
//		}

        {
	        String prefixSdk = "git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/";
	
	        
	        //map.put(prefixSdk + "evaluation-storage", esAppBuilder);
                map.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage", esAppBuilder);


	        map.put(prefixSdk + "system-adapter-mocha-jena-in-memory", saAppBuilder);		
			map.put(prefixSdk + "qpid-server", qpidServerAppBuilder);
			map.put(prefixSdk + "docker-service-manager-server", dockerServiceManagerServerAppBuilder);
        }
//        result.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0", esAppBuilder);
        //result.put("git.project-hobbit.eu:4567/cstadler/evaluationstorage/image", esAppBuilder);

        //result.put("git.project-hobbit.eu:4567/cstadler/evaluationstorage/image", esAppBuilder);

        // NOTE The sa is started by the platform
		
		//DockerServiceFactory<?> result = new DockerServiceFactorySpringApplicationBuilder(map);
		
		return map;	
	}

//	@Bean
//	public BenchmarkConfig benchmarkConfig() {
//		return FacetedBrowsingBenchmarkV2Constants.config;
//	}
	
	@Bean
	//public Map<String, Supplier<SpringApplicationBuilder>> dockerServiceFactoryOverrides() {
	public DockerServiceFactory<?> dockerServiceFactoryOverrides() {
		Map<String, Supplier<SpringApplicationBuilder>> map = ConfigVirtualDockerServiceFactoryV2.getDockerServiceFactoryOverrides(FacetedBrowsingBenchmarkV2Constants.config);

		DockerServiceFactory<?> result = new DockerServiceFactorySpringApplicationBuilder(map);
		return result;
	}
}
