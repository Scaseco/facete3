package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.service.core.BeanWrapperService;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.aksw.jena_sparql_api.ext.virtuoso.VirtuosoSystemService;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.config.ConfigBenchmarkControllerFacetedBrowsingServices;
import org.hobbit.benchmark.faceted_browsing.config.ConfigEncodersFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactorySpringApplicationBuilder;
import org.hobbit.benchmark.faceted_browsing.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.Constants;
import org.hobbit.core.component.BenchmarkControllerFacetedBrowsing;
import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.EvaluationModule;
import org.hobbit.core.component.EvaluationModuleComponent;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.data.Result;
import org.hobbit.core.service.api.IdleServiceCapable;
import org.hobbit.core.service.api.IdleServiceDelegate;
import org.hobbit.core.service.api.ServiceDelegate;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.core.storage.Storage;
import org.hobbit.core.storage.StorageInMemory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.rdf.component.SystemAdapterRDFConnection;
import org.hobbit.service.podigg.PodiggWrapper;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spotify.docker.client.exceptions.DockerCertificateException;

import io.reactivex.Flowable;


public class TestBenchmark {
//	
//	class ConfigDockerServiceManagerServiceComponent {
//		@Bean
//		public DockerServiceManagerServerComponent dockerServiceManagerServer() {
//			
//		}
//	}
//
//	class ConfigRabbitMqConnection {
//		
//	}
//	
//	class ConfigChannelRabbitMq {
//		
//	}
//		
//	public class ConfigDockerServiceManagerClientComponent {
//		@Bean
//		public DockerServiceManagerServerComponent dockerServiceManagerClient() {
//			
//		}
//	}
//	
	
//	public class ConfigCommandReceivingComponentRabbitMq {
//		@Bean
//		public Connection connectionFactory(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
//			return connectionFactory.newConnection();
//		}
//	}
	
	public static class ConfigRabbitMqConnection {
		@Bean
		public Connection commandConnection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
			return connectionFactory.newConnection();
		}
	}
	
	public static class ConfigCommandChannel {

		@Bean
		public Channel commandChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
		@Bean
		public Flowable<ByteBuffer> commandReceiver(
				Channel channel) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			return RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "cmd");
		}
		
		@Bean
		public Subscriber<ByteBuffer> commandSender(
				Channel channel) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			return RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, null);
			//return //RabbitMqFlows.createReplyableFanoutSender(channel, exchangeName, transformer)
		}
	}


	/**
	 * Creates replyable fanout sender and receiver beans over a channel
	 * 
	 * @author raven Nov 20, 2017
	 *
	 */
	public static class ConfigReplyableCommandChannel {
		@Bean
		public Flowable<SimpleReplyableMessage<ByteBuffer>> replyableCommandReceiver(
				Channel channel) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			return RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "replableCmd");
		}
		
		@Bean
		public Subscriber<ByteBuffer> replyableCommandSender(
				Channel channel) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			return RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, null);
			//return //RabbitMqFlows.createReplyableFanoutSender(channel, exchangeName, transformer)
		}
	}

	
	
	
	public static class ConfigDockerServiceManagerClient {
		
		
		@Bean
		public Function<ByteBuffer, CompletableFuture<ByteBuffer>> dockerServiceManagerConnectionClient(Channel channel) throws IOException, TimeoutException {
			return RabbitMqFlows.createReplyableFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerClient", null);
		}

		
		//@Bean(initMethod="startUp", destroyMethod="shutDown")
		//public DockerServiceManagerClientComponent dockerServiceManagerClientCore(
		@Bean
		public BeanWrapperService<ServiceDelegate<DockerServiceManagerClientComponent>> dockerServiceManagerClientCore(
				@Qualifier("commandReceiver") Flowable<ByteBuffer> commandReceiver,
				@Qualifier("dockerServiceManagerConnectionClient") Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
				Gson gson
		) throws Exception {
			DockerServiceManagerClientComponent core =
					new DockerServiceManagerClientComponent(
							commandReceiver,
							requestToServer,
							gson
					);
			return new BeanWrapperService<>(new IdleServiceDelegate<>(core));
		}
			
		//ServiceDelegate<DockerServiceManagerClientComponent>
		@Bean
		public DockerServiceBuilderFactory<?> dockerServiceManagerClient(
				//DockerServiceManagerClientComponent core
				BeanWrapperService<ServiceDelegate<DockerServiceManagerClientComponent>> tmp
		) throws Exception {
			DockerServiceManagerClientComponent core = tmp.getService().getEntity();
			
			DockerServiceBuilderFactory<DockerServiceBuilder<DockerService>> result =
					() -> DockerServiceBuilderJsonDelegate.create(core::create);
			
			return result;
		}
	}
	
	
	
	public static class ConfigDockerServiceManagerServer {
		
		// TODO: Make use of a docker service factory
		
		@Bean
		public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerConnectionServer(Channel channel) throws IOException, TimeoutException {
			return RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerServerComponent");
					//.doOnNext(x -> System.out.println("[STATUS] Received request; " + Arrays.toString(x.getValue().array()) + " replier: " + x.getReplyConsumer()));
		}

		@Bean
		public BeanWrapperService<?> dockerServiceManagerServer(
			//Supplier<? extends DockerServiceBuilder<? extends DockerService>> delegateSupplier,
			@Qualifier("commandReceiver") Flowable<ByteBuffer> commandReceiver,
			@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender,
			@Qualifier("dockerServiceManagerConnectionServer") Flowable<SimpleReplyableMessage<ByteBuffer>> requestsFromClients,
			DockerServiceFactory<?> dockerServiceFactory,
			Gson gson
		) throws DockerCertificateException {
	        
	        // Create a supplier that yields preconfigured builders
	        Supplier<DockerServiceBuilder<? extends DockerService>> builderSupplier = () -> {
	        	DockerServiceBuilder<?> r = DockerServiceBuilderJsonDelegate.create(dockerServiceFactory::create);
	        	return r;
	        };
	        
	        
	        DockerServiceManagerServerComponent service =
	        		new DockerServiceManagerServerComponent(
	        				builderSupplier,
	        				commandSender,
	        				commandReceiver,
	        				requestsFromClients,
	        				gson        				
	        				);
	        //result.startAsync().awaitRunning();

	        return new BeanWrapperService<>(service);
	        
	        //return result;
		}
		
	}


	public static class ConfigBenchmarkController {
		
		@Bean
		public BenchmarkControllerFacetedBrowsing bc() {
			return new BenchmarkControllerFacetedBrowsing();
		}
	
		@Bean
		public ApplicationRunner applicationRunner(BenchmarkControllerFacetedBrowsing controller) throws Exception {
			return (args) -> {
				controller.startUp();
				controller.run();
				controller.shutDown();
			};
		}
	}


	public static class ConfigDataGenerator {
		
		@Bean
		public Channel dg2tgChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Subscriber<ByteBuffer> dg2tgSender(@Qualifier("dg2tgChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
	    }

	    
		@Bean
		public Channel dg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> dg2saSender(@Qualifier("dg2saChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
	    }

	}
	
	
	public static class ConfigDataGeneratorFacetedBrowsing {
	    @Bean
	    public TripleStreamSupplier dataGenerationMethod() {
	        return () -> {
				try {
					return PodiggWrapper.test();
				} catch (IOException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			};
	    }

	}
	
	
	public static class ConfigTaskGenerator {

		/*
		 * Reception from dg 
		 */

		@Bean
		public Channel dg2tgChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Flowable<ByteBuffer> dg2tgReceiver(@Qualifier("dg2tgChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
	    }

		/*
		 * Transfer to sa 
		 */	    
	    
		@Bean
		public Channel tg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		
	    @Bean
	    public Subscriber<ByteBuffer> tg2saSender(@Qualifier("tg2saChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
	    }


		/*
		 * Transfer to es 
		 */	    
	    
		@Bean
		public Channel tg2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> tg2esSender(@Qualifier("tg2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	    
	    
	    /*
	     * Reception of task acknowledgements from es
	     */

		@Bean
		public Channel ackChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> taskAckReceiver(@Qualifier("ackChannel") Channel channel) throws IOException, TimeoutException {
	    	return RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_ACK_EXCHANGE_NAME, "ack");
	    }
	}
	
	public static class ConfigSystemAdapter {

		@Bean
		public Channel dg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Flowable<ByteBuffer> dg2saReceiver(@Qualifier("dg2saChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
	    }

		
		@Bean
		public RDFConnection systemUnderTestRdfConnection() {
			SparqlService tmp = FluentSparqlService.forModel().create();
			
	        //SparqlQueryConnection queryConn = new SparqlQueryConnectionJsa(tmp.getQueryExecutionFactory());
	        //SparqlUpdateConnection updateConn = new SparqlUpdateConnectionJsa(tmp.getUpdateExecutionFactory());
	        //RDFDatasetConnection
	        //RDFDatasetConnection datasetConn = new RDFDatasetConnectionVirtuoso(queryConn, sqlConn);
	        
	        //RDFConnection result = new RDFConnectionModular(queryConn, updateConn, null);

			RDFConnection result = new RDFConnectionLocal(DatasetFactory.create());
			
	        return result;
		}
		
		@Bean
		public Channel tg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2saReceiver(@Qualifier("tg2saChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> sa2esSender(@Qualifier("sa2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	}
	
	
	public static class ConfigEvaluationStorage {

		@Bean
		public Channel tg2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2esReceiver(@Qualifier("tg2esChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> sa2esReceiver(@Qualifier("sa2esChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
		

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> es2emSender(@Qualifier("es2emChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
	    }
	    
	    
		@Bean
		public Channel em2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
	    
	    @Bean
	    public Flowable<ByteBuffer> em2esReceiver(@Qualifier("em2esChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

	    
		@Bean
		public Channel ackChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Subscriber<ByteBuffer> taskAckSender(@Qualifier("ackChannel") Channel channel) throws IOException {
	    	return RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_ACK_EXCHANGE_NAME, null);
	    }		
	}
	
	
	public static class ConfigEvaluationStorageStorageProvider {
	    @Bean
	    public Storage<String, Result> storage() {
	        return new StorageInMemory<>();
	    }
	}
	
	public static class ConfigEvaluationModule {

		@Bean
		public Channel em2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Subscriber<ByteBuffer> em2esSender(@Qualifier("em2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> es2emReceiver(@Qualifier("es2emChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
	    }

	    @Bean
	    public EvaluationModule evaluationModule() {
	    	return new EvaluationModuleFacetedBrowsingBenchmark();
	    }

	}
	
	public static class BenchmarkLauncher {
		
		private static final Logger logger = LoggerFactory.getLogger(TestBenchmark.BenchmarkLauncher.class);

		
		@Bean
		public ApplicationRunner benchmarkLauncher(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
			return args -> {
				
				logger.info("Prepapring benchmark launch");
				
				// Launch the system adapter
				Service saService = dockerServiceBuilderFactory.get()
					.setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image")
					.setLocalEnvironment(ImmutableMap.<String, String>builder().build())
					.get();
				
				
				// Launch the benchmark
				Service bcService = dockerServiceBuilderFactory.get()
					.setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image")
					.setLocalEnvironment(ImmutableMap.<String, String>builder().build())
					.get();

//				Service esService = dockerServiceBuilderFactory.get()
//						.setImageName("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0")
//						.setLocalEnvironment(ImmutableMap.<String, String>builder().build())
//						.get();

//				esService.startAsync().awaitRunning();
//				esService.stopAsync().awaitTerminated(5, TimeUnit.SECONDS);
//				
//				if(true) {
//					System.out.println("yay");
//					return;
//				}
				
				saService.startAsync().awaitRunning();
				
				bcService.startAsync().awaitRunning();
				
				// Wait for the bc to finish
				bcService.awaitTerminated();
				
				
				saService.stopAsync().awaitTerminated();
			};
		}
	}

	// Configuration for the worker task generator fo the faceted browsing benchmark
	public static class ConfigTaskGeneratorFacetedBenchmark {
	    @Bean
	    public SparqlBasedSystemService taskGeneratorSparqlService() {
	        VirtuosoSystemService result = new VirtuosoSystemService(
	                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
	                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-task-generation_1112_8891/virtuoso.ini"));

	        return result;
	    }
	    
	    @Bean
	    public TaskGeneratorModule taskGeneratorModule() {
	    	return new TaskGeneratorModuleFacetedBrowsing();
	    }	    
	}

	// Configuration for the worker of the system adapter
//	public static class ConfigTaskGeneratorFacetedBenchmark {
//	    @Bean
//	    public SparqlBasedSystemService taskGeneratorSparqlService() {
//	        VirtuosoSystemService result = new VirtuosoSystemService(
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-system-under-test_1113_8892/virtuoso.ini"));
//
//		        return result;
//	    }
//	}

	
	
	
	public static class ConfigDockerServiceFactory {
		@Bean
		public DockerServiceFactory<?> dockerServiceFactory() {

			Supplier<SpringApplicationBuilder> createComponentBaseConfig = () -> new SpringApplicationBuilder()
					.sources(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigDockerServiceManagerClient.class);

			// Note: We make the actual components children of the channel configuration, so that we ensure that
			// channels are only closed once the components have shut down and sent their final messages
			Supplier<SpringApplicationBuilder> bcAppBuilder = () -> createComponentBaseConfig.get()
					.child(ConfigBenchmarkControllerFacetedBrowsingServices.class)
						.child(ConfigBenchmarkController.class);
			
			Supplier<SpringApplicationBuilder> dgAppBuilder = () -> createComponentBaseConfig.get()
					.child(ConfigDataGeneratorFacetedBrowsing.class, ConfigDataGenerator.class)
							.child(DataGeneratorFacetedBrowsing.class, LauncherServiceCapable.class);
			
			Supplier<SpringApplicationBuilder> tgAppBuilder = () -> createComponentBaseConfig.get()
					.child(ConfigEncodersFacetedBrowsing.class, ConfigTaskGenerator.class, ConfigTaskGeneratorFacetedBenchmark.class)
						.child(TaskGeneratorFacetedBenchmark.class, LauncherServiceCapable.class);

			Supplier<SpringApplicationBuilder> saAppBuilder = () -> createComponentBaseConfig.get()
					.child(ConfigEncodersFacetedBrowsing.class, ConfigSystemAdapter.class)
						.child(SystemAdapterRDFConnection.class, LauncherServiceCapable.class);
				
			Supplier<SpringApplicationBuilder> esAppBuilder = () -> createComponentBaseConfig.get()
					.child(ConfigEvaluationStorage.class, ConfigEvaluationStorageStorageProvider.class)
						.child(DefaultEvaluationStorage.class, LauncherServiceCapable.class);

			
			
			Supplier<SpringApplicationBuilder> emAppBuilder = () -> createComponentBaseConfig.get()
					.child(ConfigEvaluationModule.class)
						.child(EvaluationModuleComponent.class, LauncherServiceCapable.class);
			
			Map<String, Supplier<SpringApplicationBuilder>> map = new LinkedHashMap<>();
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image", bcAppBuilder);
			
	        map.put("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image", dgAppBuilder);
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image", tgAppBuilder);        
	        map.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0", esAppBuilder);
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image", emAppBuilder);

	        // NOTE The sa is started by the platform
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image", saAppBuilder);		
			
	        // Configure the docker server component	        
	        DockerServiceFactory<?> result = new DockerServiceFactorySpringApplicationBuilder(map);
	        

	        if(false) {
		        DockerService x = result.create("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image", ImmutableMap.<String, String>builder().build());
		        x.startAsync().awaitRunning();
		        System.out.println("SERVICE IS RUNNING");
		        x.stopAsync().awaitTerminated();
		        System.out.println("SERVICE TERMINATED");
	        }
	        
	        return result;
		}
	}
	
	@Test
	public void testBenchmark() {

		SpringApplicationBuilder builder = new SpringApplicationBuilder()
			// Add the amqp broker
			.sources(ConfigQpidBroker.class)
			// Register the docker service manager server component; for this purpose:
			// (1) Register any pseudo docker images - i.e. launchers of local components
			// (2) Configure a docker service factory - which creates service instances that can be launched
			// (3) configure the docker service manager server component which listens on the amqp infrastructure
			.child(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigDockerServiceFactory.class, ConfigDockerServiceManagerServer.class)
			.sibling(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigDockerServiceManagerClient.class, BenchmarkLauncher.class);
			;

		try(ConfigurableApplicationContext ctx = builder.run()) {}

		
//		.child(ConfigRabbitMqConnectionFactory.class)
//		// Connect the docker service factory to the amqp infrastructure 
//		.child(ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal.class, ConfigDockerServiceManagerServiceComponent.class) // Connect the local docker service factory to the rabbit mq channels
//		// Add the benchmark component
//		.sibling(ConfigBenchmarkControllerChannels.class, ConfigDockerServiceManagerClientComponent.class, ConfigHobbitFacetedBenchmarkController.class);

	}
}
