package org.hobbit.benchmark.faceted_browsing.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.service.core.BeanWrapperService;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.aksw.jena_sparql_api.core.utils.SupplierExtendedIteratorTriples;
import org.aksw.jena_sparql_api.ext.virtuoso.HealthcheckRunner;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.Constants;
import org.hobbit.core.component.EvaluationModule;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.data.Result;
import org.hobbit.core.service.api.IdleServiceDelegate;
import org.hobbit.core.service.api.ServiceDelegateEntity;
import org.hobbit.core.service.api.SparqlDockerApiService;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.core.service.docker.EnvironmentUtils;
import org.hobbit.core.storage.Storage;
import org.hobbit.core.storage.StorageInMemory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.service.podigg.PodiggWrapper;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import io.reactivex.Flowable;


public class ConfigsFacetedBrowsingBenchmark {
	
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

	public static class ConfigCommunicationWrapper
		implements EnvironmentAware {

		protected Environment env;
		
		protected String sessionId;
		protected Set<String> acceptedHeaderIds;

		
		@Bean
		public CommunicationWrapper<ByteBuffer> communicationWrapper() {			
			sessionId = env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);
			
			acceptedHeaderIds = new LinkedHashSet<>(Arrays.asList(
					sessionId,
					Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS
				));

			CommunicationWrapper<ByteBuffer> result = new CommunicationWrapperSessionId(sessionId, acceptedHeaderIds);
			return result;
		}
		
		@Override
		public void setEnvironment(Environment environment) {
			this.env = environment;						
		}
		
	}
	
//	public static class ConfigHobbitReplyableCommandWrappers
//		extends ConfigCommunicationWrapper
//	{
//		
//		@Bean
//		public Flowable<SimpleReplyableMessage<ByteBuffer>> replyableCommandReceiver(@Qualifier("replyableCommandReceiver") Flowable<SimpleReplyableMessage<ByteBuffer>> replyableCommandReceiver) throws IOException {
//			return replyableCommandReceiver
//				.flatMap(msg -> Flowable.fromIterable(wrap(msg)));
//		}
//		
//		@Bean
//		public Subscriber<ByteBuffer> replyableCommandSender(
//				@Qualifier("replyableCommandReceiver") Subscriber<ByteBuffer> replyableCommandSender) throws IOException {
//			return wrapSender(replyableCommandSender);
//		}
//	
//	}
	
//	public static class ConfigHobbitChannelWrappers
//		extends ConfigCommunicationWrapper
//	{		
//		@Bean
//		public Subscriber<ByteBuffer> commandSender(@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender) {
//			return wrapSender(commandSender);
//		}
//		
//		@Bean
//		public Flowable<ByteBuffer> commandReceiver(@Qualifier("commandReceiver") Flowable<ByteBuffer> commandReceiver) {
//			return wrapReceiver(commandReceiver);
//		}
//		
//		
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
				Channel channel, CommunicationWrapper<ByteBuffer> wrapper, @Value("${componentName:anonymous}") String componentName) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {

			//System.out.println("COMPONENT NAME: " + componentName);
			Flowable<ByteBuffer> result = RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "cmd" + "." + componentName, wrapper::wrapReceiver);
//					.flatMap(msg -> Flowable.fromIterable(wrapper.wrapReceiver(msg)));
			return result;
		}
		
		@Bean
		public Subscriber<ByteBuffer> commandSender(
				Channel channel,
				CommunicationWrapper<ByteBuffer> wrapper
				) throws IOException {
				//@Autowired(required=false) @Qualifier("foo") Function<ByteBuffer, ByteBuffer> transformer) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			
			
			Subscriber<ByteBuffer> result = RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, wrapper::wrapSender);
			return result;
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
				Channel channel,
				CommunicationWrapper<ByteBuffer> wrapper) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			Flowable<SimpleReplyableMessage<ByteBuffer>> result = RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "replyableCmd", wrapper::wrapReceiver);

			return result;
		}
		
		@Bean
		public Subscriber<ByteBuffer> replyableCommandSender(
				Channel channel,
				CommunicationWrapper<ByteBuffer> wrapper
				) throws IOException {
				//@Value("commandExchange") String commandExchange) throws IOException {
			Subscriber<ByteBuffer> result = RabbitMqFlows.createFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, wrapper::wrapSender);
			return result;
			
			//return //RabbitMqFlows.createReplyableFanoutSender(channel, exchangeName, transformer)
		}
	}

	
	
	
	public static class ConfigDockerServiceManagerClient
		implements EnvironmentAware
	{
		protected Environment env;

		
		@Bean
		public Function<ByteBuffer, CompletableFuture<ByteBuffer>> dockerServiceManagerConnectionClient(
				Channel channel,
				CommunicationWrapper<ByteBuffer> wrapper
		) throws IOException, TimeoutException {
			
			
			return RabbitMqFlows.createReplyableFanoutSender(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerClient", wrapper::wrapSender, x -> Collections.singletonList(x)); //wrapper::wrapReceiver);
		}

		
		//@Bean(initMethod="startUp", destroyMethod="shutDown")
		//public DockerServiceManagerClientComponent dockerServiceManagerClientCore(
		@Bean
		public BeanWrapperService<ServiceDelegateEntity<DockerServiceManagerClientComponent>> dockerServiceManagerClientCore(
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
				BeanWrapperService<ServiceDelegateEntity<DockerServiceManagerClientComponent>> tmp
		) throws Exception {
			DockerServiceManagerClientComponent core = tmp.getService().getEntity();
			
			DockerServiceBuilderFactory<DockerServiceBuilder<DockerService>> result =
					() -> {
			            //envVariables[envVariables.length - 2] = Constants.RABBIT_MQ_HOST_NAME_KEY + "=" + rabbitMQHostName;
			            //envVariables[envVariables.length - 1] = Constants.HOBBIT_SESSION_ID_KEY + "=" + getHobbitSessionId();

						DockerServiceBuilder<DockerService> r = DockerServiceBuilderJsonDelegate.create(core::create);
						r.getLocalEnvironment().put(Constants.HOBBIT_SESSION_ID_KEY, env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS));
						return r;
					};
			
			return result;
		}


		@Override
		public void setEnvironment(Environment environment) {
			this.env = environment;
		}
	}
	
	
//	public static class ConfigDockerServiceManagerServerConnection {
//		@Bean
//		public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerConnectionServer(Channel channel) throws IOException, TimeoutException {
//			return RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerServerComponent");
//					//.doOnNext(x -> System.out.println("[STATUS] Received request; " + Arrays.toString(x.getValue().array()) + " replier: " + x.getReplyConsumer()));
//		}		
//	}
	
//	public static class ConfigDockerServiceManagerServerConnectionWrapper
//		extends ConfigHobbitChannelWrappers
//	{
//		@Bean
//		public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerConnectionServer(@Qualifier("dockerServiceManagerConnectionServer") Flowable<SimpleReplyableMessage<ByteBuffer>> delegate) {
//			return wrap(delegate);
//		}
//	}
	
	
	public static class ConfigDockerServiceManagerServer {
		
		// TODO: Make use of a docker service factory
		
		@Bean
		public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerConnectionServer(
				Channel channel, 
				CommunicationWrapper<ByteBuffer> wrapper) throws IOException, TimeoutException {
			Flowable<SimpleReplyableMessage<ByteBuffer>> result = RabbitMqFlows.createReplyableFanoutReceiver(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "dockerServiceManagerServerComponent", wrapper::wrapReceiver);
					//.doOnNext(x -> System.out.println("[STATUS] Received request; " + Arrays.toString(x.getValue().array()) + " replier: " + x.getReplyConsumer()));
			return result;
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


//	public static class ConfigBenchmarkController {
//		
//		@Bean
//		public BenchmarkControllerFacetedBrowsing bc() {
//			return new BenchmarkControllerFacetedBrowsing();
//		}
//	
//		@Bean
//		public ApplicationRunner applicationRunner(BenchmarkControllerFacetedBrowsing controller) throws Exception {
//			return (args) -> {
//				controller.startUp();
//				controller.run();
//				controller.shutDown();
//			};
//		}
//	}


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
		public static <T> Stream<T> stream(ExtendedIterator<T> it) {
			Stream<T> result = Streams.stream(it);
			result.onClose(() -> it.close());
			return result;
		}
		
	    public static Stream<Triple> createTripleStream(String fileNameOrUrl, Lang langHint) {
	    	ExtendedIterator<Triple> it = SupplierExtendedIteratorTriples.createTripleIterator(fileNameOrUrl, langHint);
	    	Stream<Triple> result = stream(it);
	    	
	    	return result;
	    }
	    
	    @Bean
	    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
	        return () -> {
				DockerServiceBuilder<?> dockerServiceBuilder = dockerServiceBuilderFactory.get();
				DockerService podiggService = dockerServiceBuilder
					.setImageName("podigg")					
					.setLocalEnvironment(ImmutableMap.<String, String>builder().put("GTFS_GEN_SEED", "123").build())
					.get();

		    	podiggService.startAsync().awaitRunning();
		    	
		    	String host = podiggService.getContainerId();
		    	
		    	//File targetFile = new File("/tmp/podigg");
		    	String str = "http://" + host + "/podigg/latest/lc.ttl";
		    	
		    	
		    	URL url;
				try {
					url = new URL(str);
				} catch (MalformedURLException e1) {
					throw new RuntimeException(e1);
				}
		    	
		    	new HealthcheckRunner(60, 1, TimeUnit.SECONDS, () -> {
		    		try {
				        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			            connection.setRequestMethod("GET");
			            connection.connect();
			            int code = connection.getResponseCode();
			            if(code != 200) {
			            	throw new NotFoundException(url.toString());
			            }
			            connection.disconnect();
		    		} catch(Exception e) {
		    			throw new RuntimeException(e);
		    		}
 		    	}).run();
		    	
		    	
//		    	try {
//					Desktop.getDesktop().browse(new URI("http://" + host + "/podigg/latst"));
//				} catch (IOException | URISyntaxException e1) {
//					throw new RuntimeException(e1);
//				}

		    	//ByteStreams.copy(new URL(url).openStream(), new FileOutputStream(targetFile));
		    	
		    	Stream<Triple> r = createTripleStream(url.toString(), null);
		    	r.onClose(() -> {
			    	try {
						podiggService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
					} catch (TimeoutException e) {
						throw new RuntimeException();
					}
		    	});			    	
		    	
		    	return r;
			};
	    }
		
//	    @Bean
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
	    public Flowable<ByteBuffer> taskAckReceiver(@Qualifier("ackChannel") Channel channel, @Value("${componentName:anonymous}") String componentName) throws IOException, TimeoutException {
	    	return RabbitMqFlows.createFanoutReceiver(channel, Constants.HOBBIT_ACK_EXCHANGE_NAME, "ack" + "." + componentName, x -> Collections.singletonList(x));
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
	
	public static class BenchmarkLauncher
		implements EnvironmentAware
	{
		
		private static final Logger logger = LoggerFactory.getLogger(ConfigsFacetedBrowsingBenchmark.BenchmarkLauncher.class);

		protected Environment env;
		
		@Bean
		public ApplicationRunner benchmarkLauncher(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
			return args -> {
				
				logger.info("Prepapring benchmark launch");
				
				Map<String, String> serviceEnv = new HashMap<>();
				serviceEnv.put(Constants.HOBBIT_SESSION_ID_KEY, env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, "default-session"));
				
				// Launch the system adapter
				Service saService = dockerServiceBuilderFactory.get()
					.setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image")
					.setLocalEnvironment(serviceEnv)
					.get();
				
				
				// Launch the benchmark
				Service bcService = dockerServiceBuilderFactory.get()
					.setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image")
					.setLocalEnvironment(serviceEnv)
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



		@Override
		public void setEnvironment(Environment environment) {
			this.env = environment;
		}
	}
	
	public static SparqlBasedService createVirtuosoSparqlService(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
    	DockerService service = dockerServiceBuilderFactory.get()
//	    			.setImageName("tenforce/virtuoso:virtuoso7.2.4")
    			.setImageName("tenforce/virtuoso")
    			.setLocalEnvironment(ImmutableMap.<String, String>builder()
    					.put("SPARQL_UPDATE", "true")
    					.build())
    			.get();

    	SparqlBasedService result = new SparqlDockerApiService(service) {
    		protected Supplier<RDFConnection> api;
    		
    		@Override
    		protected void startUp() throws Exception {
    			super.startUp();

    			String host = delegate.getContainerId();	    			
    			String baseUrl = "http://" + host + ":" + "8890";
    			
    			api = () -> RDFConnectionFactory.connect(baseUrl + "/sparql", baseUrl + "/sparql", baseUrl + "/sparql-graph-crud/");
    			//api = () -> VirtuosoSystemService.connectVirtuoso(host, 8890, 1111);	    			
    		}
    		
    		public Supplier<RDFConnection> getApi() {
    			return api;
    		}
    	};
    	
    	return result;	    
	}

	// Configuration for the worker task generator fo the faceted browsing benchmark
	public static class ConfigTaskGeneratorFacetedBenchmark {
	    @Bean
	    public SparqlBasedService taskGeneratorSparqlService(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
	    	SparqlBasedService result = createVirtuosoSparqlService(dockerServiceBuilderFactory);
	    	return result;
	    	
//	        VirtuosoSystemService result = new VirtuosoSystemService(
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
//	                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit-task-generation_1112_8891/virtuoso.ini"));
//
//	        return result;
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
		
		private static final Logger logger = LoggerFactory.getLogger(ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceFactory.class);

		
		
		public static DockerServiceFactory<?> createSpotifyDockerClientServiceFactory(
				boolean hostMode, Map<String, String> env) throws DockerCertificateException {
	        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();


	        // Bind container port 443 to an automatically allocated available host
	        String[] ports = { }; //{ "80", "22" };
	        Map<String, List<PortBinding>> portBindings = new HashMap<>();
	        for (String port : ports) {
	            List<PortBinding> hostPorts = new ArrayList<>();
	            hostPorts.add(PortBinding.of("0.0.0.0", port));
	            portBindings.put(port, hostPorts);
	        }

	        List<PortBinding> randomPort = new ArrayList<>();
	        randomPort.add(PortBinding.randomPort("0.0.0.0"));
//	        portBindings.put("443", randomPort);

	        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

	        
	        //DockerServiceBuilderDockerClient dockerServiceFactory = new DockerServiceBuilderDockerClient();

	        //DockerServiceBuilderFactory<DockerServiceBuilder<? extends DockerService>>
	        
	        Supplier<ContainerConfig.Builder> containerConfigBuilderSupplier = () ->
	        	ContainerConfig.builder()
	        		.hostConfig(hostConfig)
	        		.env(EnvironmentUtils.mapToList("=", env))
	        		;
	        
	        Set<String> networks = Collections.singleton("hobbit");
	        
	        DockerServiceFactoryDockerClient result = new DockerServiceFactoryDockerClient(dockerClient, containerConfigBuilderSupplier, hostMode, networks);
	        return result;
		}		

		public static DockerServiceFactory<?> createDockerServiceFactory(boolean hostMode, Map<String, String> env) throws DockerCertificateException {
	        
	        // Configure the docker server component	        
	        DockerServiceFactory<?> core = createSpotifyDockerClientServiceFactory(hostMode, env);
	        

	        // FIXME Hostmode controlls two aspects which should be separated: (1) use container IPs instead of names (2) override docker images with the component registry
	        if(hostMode) {	        
	        	DockerServiceFactory<?> localOverrides = ConfigVirtualDockerServiceFactory.createVirtualComponentDockerServiceFactory();
	        	core = new DockerServiceFactoryChain(localOverrides, core);	        
	        }
	        
	        DockerServiceFactory<?> result = ConfigVirtualDockerServiceFactory.applyServiceWrappers(core);
	        
	        // Test starting a triple store and use it
	        if(false) {
	        	
		        DockerService service = result.create("tenforce/virtuoso", ImmutableMap.<String, String>builder().build());
		        service.startAsync().awaitRunning();
		        String name = service.getContainerId();	        
		        String url = "http://" + name + ":8890/sparql";
		        System.out.println("url: <" + url + ">");
		        
				try {
					System.out.println(CharStreams.toString(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
		        service.stopAsync().awaitTerminated();
				System.exit(0);
	

	        }
	        
	        if(false) {
		        DockerService x = result.create("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image", ImmutableMap.<String, String>builder().build());
		        x.startAsync().awaitRunning();
		        System.out.println("SERVICE IS RUNNING");
		        x.stopAsync().awaitTerminated();
		        System.out.println("SERVICE TERMINATED");
	        }
	        
	        return result;			
		}
		
		@Bean
		public DockerServiceFactory<?> dockerServiceFactory(@Value("${hostMode:false}") boolean hostMode, @Value("${HOBBIT_RABBIT_HOST:localhost}") String envStr) throws DockerCertificateException {
			Map<String, String> env = new ImmutableMap.Builder<String, String>()
					.put("HOBBIT_RABBIT_HOST", envStr)
					.build();
			
			DockerServiceFactory<?> result = createDockerServiceFactory(hostMode, env);
			return result;
		}
	}
}
