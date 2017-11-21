package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hobbit.benchmark.faceted_browsing.config.ConfigBenchmarkControllerFacetedBrowsingServices;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal;
import org.hobbit.benchmark.faceted_browsing.config.DockerServiceFactoryUtilsSpringBoot;
import org.hobbit.benchmark.faceted_browsing.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.Constants;
import org.hobbit.core.component.BenchmarkControllerFacetedBrowsing;
import org.hobbit.core.component.DataGeneratorFacetedBrowsing;
import org.hobbit.core.component.DefaultEvaluationStorage;
import org.hobbit.core.component.TaskGeneratorFacetedBenchmark;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.hobbit.rdf.component.SystemAdapterRDFConnection;
import org.junit.Test;
import org.reactivestreams.Subscriber;
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
			return RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME);
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
			return RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME);
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
			return RabbitMqFlows.createReplyableFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, null);
		}

		
		@Bean(initMethod="startUp", destroyMethod="shutDown")
		public DockerServiceManagerClientComponent dockerServiceManagerClientCore(
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
			return core;
		}
			
		@Bean
		public DockerServiceBuilderFactory<?> dockerServiceManagerClient(
				DockerServiceManagerClientComponent core
		) throws Exception {
			
			DockerServiceBuilderFactory<DockerServiceBuilder<DockerService>> result =
					() -> DockerServiceBuilderJsonDelegate.create(core::create);
			
			return result;
		}
	}
	
	
	
	public static class ConfigDockerServiceManagerServer {
		
		// TODO: Make use of a docker service factory
		
		@Bean
		public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerConnectionServer(Channel channel) throws IOException, TimeoutException {
			return RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME);
					//.doOnNext(x -> System.out.println("[STATUS] Received request; " + Arrays.toString(x.getValue().array()) + " replier: " + x.getReplyConsumer()));
		}

		@Bean
		public Service dockerServiceManagerServer(
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
	        
	        
	        DockerServiceManagerServerComponent result =
	        		new DockerServiceManagerServerComponent(
	        				builderSupplier,
	        				commandSender,
	        				commandReceiver,
	        				requestsFromClients,
	        				gson        				
	        				);
	        result.startAsync().awaitRunning();

	        return result;
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
	    public Subscriber<ByteBuffer> dg2tg(@Qualifier("dg2tgChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
	    }

	    
		@Bean
		public Channel dg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> dg2sa(@Qualifier("dg2saChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
	    }

	    @Bean
	    public Flowable<ByteBuffer> dg2saPub(@Qualifier("dg2saChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
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
	    public Flowable<ByteBuffer> dg2tgPub(@Qualifier("dg2tgChannel") Channel channel) throws IOException, TimeoutException {
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
	    public Subscriber<ByteBuffer> tg2sa(@Qualifier("tg2saChannel") Channel channel) throws IOException {
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
	    public Subscriber<ByteBuffer> tg2es(@Qualifier("tg2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	    
	    
	    /*
	     * Reception of task acknowledgements from es
	     */
	    
	    @Bean
	    public Flowable<ByteBuffer> taskAckPub(@Qualifier("ackChannel") Channel channel) throws IOException, TimeoutException {
	    	return RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_ACK_EXCHANGE_NAME);
	    }
	}
	
	public static class ConfigSystemAdapter {

		@Bean
		public Channel tg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2saPub(@Qualifier("tg2saChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> sa2es(@Qualifier("sa2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	}
	
	
	public static class ConfigEvaluationStorage {

		@Bean
		public Channel tg2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2esPub(@Qualifier("tg2esChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> sa2esPub(@Qualifier("sa2esChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
		

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> es2em(@Qualifier("sa2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
	    }
	    
	    @Bean
	    public Flowable<ByteBuffer> em2esPub(@Qualifier("em2esChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

	    
		@Bean
		public Channel ackChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Subscriber<ByteBuffer> taskAck(@Qualifier("ackChannel") Channel channel) throws IOException {
	    	return RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_ACK_EXCHANGE_NAME, null);
	    }		
	}
	
	public static class ConfigEvaluationModule {

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}


	    @Bean
	    public Subscriber<ByteBuffer> em2es(@Qualifier("em2esChannel") Channel channel) throws IOException {
	        return RabbitMqFlows.createDataSender(channel, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

	    
	    @Bean
	    public Flowable<ByteBuffer> es2emPub(@Qualifier("es2emChannel") Channel channel) throws IOException, TimeoutException {
	        return RabbitMqFlows.createDataReceiver(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
	    }

	}
	
	
	public static class BenchmarkLauncher {
		@Bean
		public ApplicationRunner benchmarkLauncher(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
			return args -> {
				
				System.out.println("Launching benchmark");
				
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
				
				saService.startAsync().awaitRunning();
				
				bcService.startAsync().awaitRunning();
				
				// Wait for the bc to finish
				bcService.awaitTerminated();
				
				saService.stopAsync().awaitTerminated();
			};
		}
	}

	
	public static class ConfigDockerServiceFactory {
		@Bean
		public DockerServiceFactory<?> dockerServiceFactory() {

			Supplier<SpringApplicationBuilder> createComponentBaseConfig = () -> new SpringApplicationBuilder()
					.sources(ConfigRabbitMqConnectionFactory.class, ConfigCommandChannel.class, ConfigDockerServiceManagerClient.class);
					
			Supplier<SpringApplicationBuilder> bcAppBuilder = () -> createComponentBaseConfig.get()
					.sources(ConfigBenchmarkControllerFacetedBrowsingServices.class, ConfigBenchmarkController.class);
			
			Supplier<SpringApplicationBuilder> dgAppBuilder = () -> createComponentBaseConfig.get()
					.sources(ConfigDataGenerator.class, DataGeneratorFacetedBrowsing.class);
			
			Supplier<SpringApplicationBuilder> tgAppBuilder = () -> createComponentBaseConfig.get()
					.sources(ConfigTaskGenerator.class, TaskGeneratorFacetedBenchmark.class);

			Supplier<SpringApplicationBuilder> saAppBuilder = () -> createComponentBaseConfig.get()
					.sources(ConfigSystemAdapter.class, SystemAdapterRDFConnection.class);
				
			Supplier<SpringApplicationBuilder> esAppBuilder = () -> createComponentBaseConfig.get()
					.sources(ConfigEvaluationModule.class, DefaultEvaluationStorage.class);

			Supplier<SpringApplicationBuilder> emAppBuilder = () -> createComponentBaseConfig.get()
					.sources(ConfigEvaluationModule.class, EvaluationModuleFacetedBrowsingBenchmark.class);

			
			Map<String, Supplier<SpringApplicationBuilder>> map = new LinkedHashMap<>();
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image", bcAppBuilder);
			
	        map.put("git.project-hobbit.eu:4567/gkatsibras/faceteddatagenerator/image", dgAppBuilder);
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image", tgAppBuilder);        
	        map.put("git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage:1.0.0", esAppBuilder);
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image", emAppBuilder);

	        // NOTE The sa is started by the platform
	        map.put("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image", saAppBuilder);		
			
	        // Configure the docker server component
			
	        DockerServiceFactory<?> result = DockerServiceFactoryUtilsSpringBoot.createDockerServiceFactoryForBootstrap(map);
			return result;
			//DockerServiceBuilder dockerServiceBuilder = DockerServiceBuilderJsonDelegate.create(dockerServiceFactory::create);
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
			.child(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommandChannel.class, ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal.class, ConfigDockerServiceFactory.class, ConfigDockerServiceManagerServer.class)
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
