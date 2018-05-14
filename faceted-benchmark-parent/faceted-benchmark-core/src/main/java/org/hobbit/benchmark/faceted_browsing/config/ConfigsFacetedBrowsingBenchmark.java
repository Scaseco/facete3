package org.hobbit.benchmark.faceted_browsing.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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

import javax.inject.Inject;

import org.aksw.commons.service.core.BeanWrapperService;
import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.aksw.jena_sparql_api.core.utils.SupplierExtendedIteratorTriples;
import org.aksw.jena_sparql_api.ext.virtuoso.HealthcheckRunner;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.hobbit.benchmark.faceted_browsing.component.TaskGeneratorModuleFacetedBrowsing;
import org.hobbit.benchmark.faceted_browsing.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.component.EvaluationModule;
import org.hobbit.core.component.TaskGeneratorModule;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.data.Result;
import org.hobbit.core.rabbit.RabbitMQUtils;
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
import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;
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
			
		private static final Logger logger = LoggerFactory
				.getLogger(ConfigsFacetedBrowsingBenchmark.ConfigCommunicationWrapper.class);

		
		protected Environment env;
		
		protected String sessionId;
		protected Set<String> acceptedHeaderIds;

		
		@Bean
		public CommunicationWrapper<ByteBuffer> communicationWrapper() {			
			sessionId = env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY);//, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);
			//sessionId = env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);
			logger.info("SessionId obtained from the environment is: " + sessionId);
			
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
	
	public static class ConfigQueueNameMapper {	    
	    @Bean
	    public Function<String, String> queueNameMapper(@Value("${" + Constants.HOBBIT_SESSION_ID_KEY + ":" + Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS + "}") String hobbitSessionId) {
	        return queueName -> queueName + "." + hobbitSessionId;
	    }
	}
	
	public static interface DataQueueFactory {
	    Subscriber<ByteBuffer> createSender(Channel channel, String queueName) throws Exception;
	    Flowable<ByteBuffer> createReceiver(Channel channel, String queueName) throws Exception;	    
	}
	
	public static class DataQueueFactoryImpl
           implements DataQueueFactory
    {
       @Override
       public Subscriber<ByteBuffer> createSender(Channel channel, String queueName) throws IOException {
           return RabbitMqFlows.createDataSender(channel, queueName);           
       }

       @Override
       public Flowable<ByteBuffer> createReceiver(Channel channel, String queueName) throws IOException, TimeoutException {
           return RabbitMqFlows.createDataReceiver(channel, queueName);           
       }
   }

	
	public static class DataQueueFactoryRenaming
	    implements DataQueueFactory
	{
        protected DataQueueFactory delegate;
	    protected Function<String, String> queueNameMapper;
	    
        public DataQueueFactoryRenaming(DataQueueFactory delegate,
                Function<String, String> queueNameMapper) {
            super();
            this.delegate = delegate;
            this.queueNameMapper = queueNameMapper;
        }

        @Override
        public Subscriber<ByteBuffer> createSender(Channel channel, String baseQueueName) throws Exception {
            String queueName = queueNameMapper.apply(baseQueueName);
            return delegate.createSender(channel, queueName);            
        }

        @Override
        public Flowable<ByteBuffer> createReceiver(Channel channel, String baseQueueName) throws Exception {
            String queueName = queueNameMapper.apply(baseQueueName);
            return delegate.createReceiver(channel, queueName);            
        }
	}
	
	public static class ConfigDataQueueFactory {
	    @Bean
	    public DataQueueFactory dataQueueFactory(@Qualifier("queueNameMapper") Function<String, String> queueNameMapper) {
	        DataQueueFactory result = new DataQueueFactoryImpl();
	        
	        if(queueNameMapper != null) {
	            result = new DataQueueFactoryRenaming(result, queueNameMapper);
	        }
	        
	        return result;
	    }
	    
	}
	
	
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
		private static final Logger logger = LoggerFactory
				.getLogger(ConfigsFacetedBrowsingBenchmark.ConfigDockerServiceManagerClient.class);

		
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
		public BeanWrapperService<DockerServiceManagerClientComponent> dockerServiceManagerClientCore(
				@Qualifier("commandReceiver") Flowable<ByteBuffer> commandReceiver,
				@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender,
				@Qualifier("dockerServiceManagerConnectionClient") Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
				Gson gson,
                @Value("${" + Constants.CONTAINER_NAME_KEY + ":no-requester-container-id-set}") String requesterContainerId,
                @Value("${" + ConfigVirtualDockerServiceFactory.DEFAULT_REQUESTED_CONTAINER_TYPE_KEY + ":no-default-requested-container-type-set}") String defaultRequestedContainerType				
		) throws Exception {			
			
			DockerServiceManagerClientComponent core =
					new DockerServiceManagerClientComponent(
							commandReceiver,
							commandSender,
							requestToServer,
							gson,
							requesterContainerId,
							defaultRequestedContainerType
					);
			
			BeanWrapperService<DockerServiceManagerClientComponent> result = new BeanWrapperService<>(core);
			return result;
		}
			
		//ServiceDelegate<DockerServiceManagerClientComponent>
		@Bean
		public DockerServiceBuilderFactory<?> dockerServiceManagerClient(
				//DockerServiceManagerClientComponent core
				BeanWrapperService<DockerServiceManagerClientComponent> tmp,
                @Value("${" + Constants.HOBBIT_SESSION_ID_KEY + ":" + Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS + "}") String hobbitSessionId,                
                @Value("${" + Constants.RABBIT_MQ_HOST_NAME_KEY + ":localhost}") String amqpHost                
				
		) throws Exception {
			DockerServiceManagerClientComponent tmpCore = tmp.getService();
			
			// Apply client side service wrappers
			DockerServiceFactory<?> core = ConfigVirtualDockerServiceFactory.applyServiceWrappers(tmpCore);
			
			DockerServiceBuilderFactory<DockerServiceBuilder<DockerService>> result =
					() -> {
			            //envVariables[envVariables.length - 2] = Constants.RABBIT_MQ_HOST_NAME_KEY + "=" + rabbitMQHostName;
			            //envVariables[envVariables.length - 1] = Constants.HOBBIT_SESSION_ID_KEY + "=" + getHobbitSessionId();

						DockerServiceBuilderJsonDelegate<DockerService> r = DockerServiceBuilderJsonDelegate.create(core::create);
						//r.getBaseEnvironment().put(Constants.HOBBIT_SESSION_ID_KEY, env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS));
						r.getBaseEnvironment().put(Constants.HOBBIT_SESSION_ID_KEY, hobbitSessionId);
                        r.getBaseEnvironment().put(Constants.RABBIT_MQ_HOST_NAME_KEY, amqpHost);
						
						
						logger.info("Prepared docker service builder with base configuration: " + r.getBaseEnvironment());
						
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
		
        @Inject
        protected DataQueueFactory dataQueueFactory;

		@Bean
		public Channel dg2tgChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Subscriber<ByteBuffer> dg2tgSender(@Qualifier("dg2tgChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createSender(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
	    }

	    
		@Bean
		public Channel dg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> dg2saSender(@Qualifier("dg2saChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createSender(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
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
	    
	    public static Stream<Triple> createPodiggDatasetViaDocker(
	    		DockerServiceBuilderFactory<?> dockerServiceBuilderFactory,
	    		String imageName,
	    		Map<String, String> env) {
			DockerServiceBuilder<?> dockerServiceBuilder = dockerServiceBuilderFactory.get();
			DockerService podiggService = dockerServiceBuilder
				.setImageName(imageName)					
				.setLocalEnvironment(env)
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
	    	
			// TODO Ensure the health check can be interrupted on service stop 
	    	new HealthcheckRunner(60 * 15, 1, TimeUnit.SECONDS, () -> {
	    		try {
			        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		            connection.setRequestMethod("GET");
		            connection.connect();
		            int code = connection.getResponseCode();
		            if(code != 200) {
		            	logger.info("Health check status: fail");
		            	throw new NotFoundException(url.toString());
		            }
		            connection.disconnect();
	            	logger.info("Health check status: success");
	    		} catch(Exception e) {
	    			throw new RuntimeException(e);
	    		}
		    	}).run();
	    	
	    	
//	    	try {
//				Desktop.getDesktop().browse(new URI("http://" + host + "/podigg/latst"));
//			} catch (IOException | URISyntaxException e1) {
//				throw new RuntimeException(e1);
//			}

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
	    }
	    
	    @Bean
	    public TripleStreamSupplier dataGenerationMethod() {
	        logger.info("*** DG: USING STATIC TEST DATASET - DO NOT USE FOR PRODUCTION ***");
	    	return () -> {
		    	ExtendedIterator<Triple> it = RDFDataMgr.loadModel("podigg-lc-small.ttl").getGraph().find();
	    		return Streams.stream(it).onClose(it::close);
	    	};
	    }
	    
	    //@Bean
	    public TripleStreamSupplier dataGenerationMethodX(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
	        
	        logger.info("DG: Supplied param model is: " + paramModelStr);
	        
	        // Load the benchmark.ttl config as it contains the parameter mapping
	        Model paramModel = ModelFactory.createDefaultModel();
	        RDFDataMgr.read(paramModel, new ByteArrayInputStream(paramModelStr.getBytes()), Lang.JSONLD);

	        Model meta = RDFDataMgr.loadModel("benchmark.ttl");
	        
	        Model model = ModelFactory.createDefaultModel();
	        model.add(paramModel);
	        model.add(meta);

	        
	        // Check if the quickTestRun parameter is set to true - if so, use test settings
	        Property pQueryTestRun = ResourceFactory.createProperty("http://w3id.org/bench#paramQuickTestRun");
	        
	        boolean isParamModelEmpty = paramModel.isEmpty(); 
	        boolean isQuickTestRunSet = model.listStatements(null, pQueryTestRun, (RDFNode)null).nextOptional().map(Statement::getBoolean).orElse(false);

	        boolean doQuickRun = false;
	        if(isParamModelEmpty) {
		        logger.warn("*** TEST RUN FLAG HAS BEEN AUTOMATICALLY SET BECAUSE NO BENCHMARK PARAMETERS WERE PROVIDED - MAKE SURE THAT THIS IS EXPECTED ***");
	        	doQuickRun = true;
	        } else if(isQuickTestRunSet) {
		        logger.warn("*** TEST RUN FLAG WAS MANUALLY SET - ANY OTHER PROVIDED BENCHMARK PARAMETERS ARE IGNORED - MAKE SURE THAT THIS IS EXPECTED ***");
		        doQuickRun = true;
	        }	        
	        
	        
	        Map<String, String> params = new HashMap<>();
	        
	        
	        if(doQuickRun) {

                params.put("GTFS_GEN_SEED", "111");
//		        params = ImmutableMap.<String, String>builder()
//	                .put("GTFS_GEN_SEED", "111")
//	                .put("GTFS_GEN_REGION__SIZE_X", "2000")
//	                .put("GTFS_GEN_REGION__SIZE_Y", "2000")
//	                .put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
//	                .put("GTFS_GEN_STOPS__STOPS", "4000")
//	                .put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
//	                .put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "900000")
//	                .put("GTFS_GEN_ROUTES__ROUTES", "4000")
//	                .put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
//	                .put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
//	                .put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1")
//	                .put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "31536000000")
//	                .build();                               
                
	        } else {

		        Property pOption = ResourceFactory.createProperty("http://w3id.org/bench#podiggOption");
		        List<Resource> props = model.listSubjectsWithProperty(pOption).toList();
		        for(Resource p : props) {
		            String key = p.getProperty(pOption).getString();
	
		            Property pp = ResourceFactory.createProperty(p.getURI());
		            List<RDFNode> values = model.listObjectsOfProperty(pp).toList();
	
	                logger.info("DG: Values of " + pp + " " + values);
	
	                if(values.size() > 1) {
		                throw new RuntimeException("Too many values; at most one expected for " + pp + ": " + values);
		            }
	
	                if(!values.isEmpty()) {
	                    RDFNode o = values.get(0);
	                    String val = o.asNode().getLiteralLexicalForm();
	                    
	                    params.put(key, val);
	                }	            
		        }
		    }
	        
//	        logger.info("Podigg options: " + params);
//	        
//	        params = ImmutableMap.<String, String>builder()
//	                .put("GTFS_GEN_SEED", "111")
//	                .put("GTFS_GEN_REGION__SIZE_X", "2000")
//	                .put("GTFS_GEN_REGION__SIZE_Y", "2000")
//	                .put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
//	                .put("GTFS_GEN_STOPS__STOPS", "3000")
//	                .put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
//	                .put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "200000")
//	                .put("GTFS_GEN_ROUTES__ROUTES", "1000")
//	                .put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
//	                .put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
//	                .put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1")
//	                .put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "31536000000")
//	                .build();
//
//	        params = new HashMap<>();
	        
	        logger.info("DG: Configuring podigg with parameters: " + params);

	        
	    	//String imageName = "podigg";
	    	String imageName = "git.project-hobbit.eu:4567/cstadler/podigg/image";
	    	Map<String, String> env = ImmutableMap.<String, String>builder().putAll(params).build();
	    	return () -> createPodiggDatasetViaDocker(dockerServiceBuilderFactory, imageName, env);
	    }
		
//	    @Bean
	    public TripleStreamSupplier dataGenerationMethodOld() {
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

        @Inject
        protected DataQueueFactory dataQueueFactory;

		/*
		 * Reception from dg 
		 */

		@Bean
		public Channel dg2tgChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Flowable<ByteBuffer> dg2tgReceiver(@Qualifier("dg2tgChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
	    }

		/*
		 * Transfer to sa 
		 */	    
	    
		@Bean
		public Channel tg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		
	    @Bean
	    public Subscriber<ByteBuffer> tg2saSender(@Qualifier("tg2saChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createSender(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
	    }


		/*
		 * Transfer to es 
		 */	    
	    
		@Bean
		public Channel tg2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> tg2esSender(@Qualifier("tg2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createSender(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	    
	    
	    /*
	     * Reception of task acknowledgements from es
	     */

		@Bean
		public Channel ackChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> taskAckReceiver(@Qualifier("ackChannel") Channel channel, @Value("${componentName:anonymous}") String componentName, @Qualifier("queueNameMapper") Function<String, String> queueNameMapper) throws IOException, TimeoutException {
	    	String queueName = queueNameMapper.apply(Constants.HOBBIT_ACK_EXCHANGE_NAME);
	    	return RabbitMqFlows.createFanoutReceiver(channel, queueName, "ack" + "." + componentName, x -> Collections.singletonList(x));
	    }
	}
	
	public static class ConfigSystemAdapter {

	    @Inject
	    protected DataQueueFactory dataQueueFactory;
	    
		@Bean
		public Channel dg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Flowable<ByteBuffer> dg2saReceiver(@Qualifier("dg2saChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
	    }

        //SparqlQueryConnection queryConn = new SparqlQueryConnectionJsa(tmp.getQueryExecutionFactory());
        //SparqlUpdateConnection updateConn = new SparqlUpdateConnectionJsa(tmp.getUpdateExecutionFactory());
        //RDFDatasetConnection
        //RDFDatasetConnection datasetConn = new RDFDatasetConnectionVirtuoso(queryConn, sqlConn);
        
        //RDFConnection result = new RDFConnectionModular(queryConn, updateConn, null);

		
	    // Jena
		@Bean
		public RDFConnection systemUnderTestRdfConnection() {
			//SparqlService tmp = FluentSparqlService.forModel().create();
			//RDFConnection result = new RDFConnectionLocal(DatasetFactory.create());

			RDFConnection result = RDFConnectionFactory.connect(DatasetFactory.create());
	        return result;
		}


	    // Virtuoso
	    //@Bean
		public RDFConnection systemUnderTestRdfConnection(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
		    	SparqlBasedService service = createVirtuosoSparqlService(dockerServiceBuilderFactory);
		    	service.startAsync().awaitRunning();
//		    	return result;
//		    	
//	    	DockerService service = dockerClient.create("tenforce/virtuoso", null);
//
//			service.startAsync().awaitRunning();
//			String host = service.getContainerId();
//        	String url = "http://" + host + ":8890/";
		    	RDFConnection result = service.createDefaultConnection();
	        //RDFConnection result = RDFConnectionFactory.connect(url);

			
//	    	SparqlService tmp = FluentSparqlService.forModel().create();
//			RDFConnection result = new RDFConnectionLocal(DatasetFactory.create());
			
	        return result;
		}

		
		@Bean
		public Channel tg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2saReceiver(@Qualifier("tg2saChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> sa2esSender(@Qualifier("sa2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createSender(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	}
	
	
	public static class ConfigEvaluationStorage {

        @Inject
        protected DataQueueFactory dataQueueFactory;

		@Bean
		public Channel tg2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2esReceiver(@Qualifier("tg2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> sa2esReceiver(@Qualifier("sa2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
		

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

//	    @Bean
//	    public Subscriber<ByteBuffer> es2emSender(@Qualifier("es2emChannel") Channel channel) throws Exception {
//	        return dataQueueFactory.createSender(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
//	    }
	    
	    
		@Bean
		public Channel em2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
	    
//	    @Bean
//	    public Flowable<ByteBuffer> em2esReceiver(@Qualifier("em2esChannel") Channel channel) throws Exception {
//	        return dataQueueFactory.createReceiver(channel, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//	    }

		
		//@Qualifier("requestQueueFactory")
	    @Bean
	    public Flowable<SimpleReplyableMessage<ByteBuffer>> es2emServer(
	    		@Qualifier("em2esChannel") Channel channel,
	    		@Qualifier("queueNameMapper") Function<String, String> queueNameMapper,
	            @Value("${" + Constants.EVAL_MODULE_2_EVAL_STORAGE_QUEUE_NAME_KEY + ":" + Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME + "}") String baseQueueName
	    		) throws Exception {
            String queueName = queueNameMapper.apply(baseQueueName);
	    	
            channel.queueDeclare(queueName, false, false, true, null);
            return RabbitMqFlows.createFlowableForQueue(() -> channel, c -> queueName);            
	    }

	    
		@Bean
		public Channel ackChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Subscriber<ByteBuffer> taskAckSender(@Qualifier("ackChannel") Channel channel, @Qualifier("queueNameMapper") Function<String, String> queueNameMapper) throws IOException {
			String queueName = queueNameMapper.apply(Constants.HOBBIT_ACK_EXCHANGE_NAME);
            channel.exchangeDeclare(queueName, "fanout", false, true, null);
            
            return RabbitMqFlows.wrapPublishAsSubscriber(
            		RabbitMqFlows.wrapPublishAsConsumer(channel, queueName, "", null),
            		() -> 0);
			
			//return RabbitMqFlows.createFanoutSender(channel, queueName, null);
	    }		
	}
	
	
	public static class ConfigEvaluationStorageStorageProvider {
	    @Bean
	    public Storage<String, Result> storage() {
	        return new StorageInMemory<>();
	    }
	}
	
	public static class ConfigEvaluationModule {

        @Inject
        protected DataQueueFactory dataQueueFactory;

		@Bean
		public Channel em2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
		public Channel es2emChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

		@Bean
	    public Function<ByteBuffer, CompletableFuture<ByteBuffer>> em2esClient(
	    		@Qualifier("em2esChannel") Channel em2esChannel,
	    		@Qualifier("es2emChannel") Channel es2emChannel,
	    		@Qualifier("queueNameMapper") Function<String, String> queueNameMapper) throws Exception {

			String em2esQueueName = queueNameMapper.apply(Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
			String es2emQueueName = queueNameMapper.apply(Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);

			Subscriber<ByteBuffer> sender = RabbitMqFlows.createDataSender(em2esChannel, em2esQueueName, es2emQueueName);
			Flowable<ByteBuffer> receiver = RabbitMqFlows.createDataReceiver(es2emChannel, es2emQueueName);
			
			Function<ByteBuffer, CompletableFuture<ByteBuffer>> result = RabbitMqFlows.wrapAsFunction(sender, receiver);

			return result;
		}


//	    @Bean
//	    public Flowable<ByteBuffer> es2emReceiver(@Qualifier("es2emChannel") Channel channel) throws Exception {
//	        return dataQueueFactory.createReceiver(channel, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
//	    }

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
		
		
		
		
		
		// Hack from https://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
		public static void setEnv(Map<String, String> newenv) throws Exception {
		    try {
		      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
		      theEnvironmentField.setAccessible(true);
		      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
		      env.putAll(newenv);
		      Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
		      theCaseInsensitiveEnvironmentField.setAccessible(true);
		      Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
		      cienv.putAll(newenv);
		    } catch (NoSuchFieldException e) {
		      Class[] classes = Collections.class.getDeclaredClasses();
		      Map<String, String> env = System.getenv();
		      for(Class cl : classes) {
		        if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
		          Field field = cl.getDeclaredField("m");
		          field.setAccessible(true);
		          Object obj = field.get(env);
		          Map<String, String> map = (Map<String, String>) obj;
		          map.clear();
		          map.putAll(newenv);
		        }
		      }
		    }
		  }
		public void mockSa() throws Exception {
		    Map<String, String> map = new HashMap<>(System.getenv());
		    map.put(Constants.HOBBIT_SESSION_ID_KEY, env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY));
		    map.put(Constants.RABBIT_MQ_HOST_NAME_KEY, env.getProperty(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost"));
		    setEnv(map);
		    
		    AbstractSystemAdapter asa = new AbstractSystemAdapter() {
                
                @Override
                public void receiveGeneratedTask(String taskId, byte[] data) {
                	
                	String queryString = RabbitMQUtils.readString(data);
                    System.out.println("SA Received task " + taskId + ": " + queryString);
                    try {
						sendResultToEvalStorage(taskId, RabbitMQUtils.writeString(""));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
                }
                
                @Override
                public void receiveGeneratedData(byte[] data) {
                	
                    System.out.println("SA Received some data");
                    
                }
            };
            

            asa.init();
		}
		
		
		
		@Bean
		public ApplicationRunner benchmarkLauncher(
				DockerServiceBuilderFactory<?> dockerServiceBuilderFactory,
				@Qualifier("commandSender") Subscriber<ByteBuffer> commandSender		
		) {
			return args -> {
				try {
					logger.info("BenchmarkLauncher starting");
					
					// The service builder factory is pre-configured to set Constants.HOBBIT_SESSION_ID_KEY
					
					Map<String, String> serviceEnv = new HashMap<>();
					serviceEnv.put(Constants.HOBBIT_SESSION_ID_KEY, env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY));
					
					// Launch the system adapter
					DockerService saService = dockerServiceBuilderFactory.get()
						.setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image")
						//.setLocalEnvironment(serviceEnv)
						.get();
					
					
					// Launch the benchmark
					Service bcService = dockerServiceBuilderFactory.get()
						.setImageName("git.project-hobbit.eu:4567/gkatsibras/facetedbenchmarkcontroller/image")
						//.setLocalEnvironment(serviceEnv)
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
					
					//mockAbstractSa();

					// Ensure that both the SA and BC are healthy before sending out the start signal
					// This ensures that the BC is ready to accept the start event
					ServiceManager serviceManager = new ServiceManager(Arrays.asList(saService, bcService));
					serviceManager.addListener(new Listener() {
						@Override
						public void healthy() {
							String saContainerId = saService.getContainerId();
							commandSender.onNext(ByteBuffer.wrap(Bytes.concat(
								new byte[] {Commands.START_BENCHMARK_SIGNAL},
								saContainerId.getBytes(StandardCharsets.UTF_8)
							)));							
						}
					});

					serviceManager.startAsync();
					try {					
						// Wait for the bc to finish
						bcService.awaitTerminated();
					} finally {
						saService.awaitTerminated(10, TimeUnit.SECONDS);
					}
				} finally {
					logger.info("BenchmarkLauncher terminating");
				}
			};
		}



		@Override
		public void setEnvironment(Environment environment) {
			this.env = environment;
		}
	}
	
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigsFacetedBrowsingBenchmark.class);

	// TODO I think this method is no longer needed, as the service wrapping is done by
	// applySericeWrappers
	public static SparqlBasedService createVirtuosoSparqlService(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
    	DockerService service = dockerServiceBuilderFactory.get()
//	    			.setImageName("tenforce/virtuoso:virtuoso7.2.4")
    			.setImageName("tenforce/virtuoso")
    			.setLocalEnvironment(ImmutableMap.<String, String>builder()
    					.put("SPARQL_UPDATE", "true")
    					.build())
    			.get();

    	SparqlBasedService result = new SparqlDockerApiService(service) {
    		public Supplier<RDFConnection> getApi() {
    			Supplier<RDFConnection> result;
    			if(isRunning()) {
        			String host = delegate.getContainerId();
        			String baseUrl = "http://" + host + ":" + "8890";
        			
        			result = () -> RDFConnectionFactory.connect(baseUrl + "/sparql", baseUrl + "/sparql", baseUrl + "/sparql-graph-crud/");
        			
        			//result = () -> RDFConnectionFactory.connect(baseUrl);
        			
        			logger.info("Sparql endpoint online at: " + baseUrl);
        		} else {
        			throw new IllegalStateException("Can only access API of running services");
        		}
    			return result;
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
