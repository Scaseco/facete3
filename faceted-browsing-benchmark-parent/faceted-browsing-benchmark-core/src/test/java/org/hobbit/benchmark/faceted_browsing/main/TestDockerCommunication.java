package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hobbit.benchmark.faceted_browsing.config.ConfigCommunicationWrapper;
import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceBuilder;
import org.hobbit.core.service.docker.api.DockerServiceSystem;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderJsonDelegate;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceSystemDockerClient;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.google.common.collect.ImmutableMap;
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

public class TestDockerCommunication {
	
	protected static final String commandExchange = Constants.HOBBIT_COMMAND_EXCHANGE_NAME;

	public static class CommonContext {
		@Bean
		public Connection connection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
//			System.out.println("[STATUS] Creating connection from ConnectionFactory " + connectionFactory);
			Connection result = connectionFactory.newConnection();
//			result.addShutdownListener((t) -> { System.out.println("[STATUS] Closing connection from ConnectionFactory " + connectionFactory); });
			return result;
		}

		@Bean(destroyMethod="close")
		public Channel channel(Connection connection) throws IOException {
//			System.out.println("[STATUS] Creating channel from Connection " + connection);
			Channel result = connection.createChannel();
			result.addShutdownListener((t) -> {
				System.out.println("[STATUS] Closing channel " + result + "[" + result.hashCode() + "] from Connection " + connection + " " + connection.hashCode());
			});
			return result;
		}

		@Bean
		public Subscriber<ByteBuffer> commandChannel(Channel channel, CommunicationWrapper<ByteBuffer> wrapper) throws IOException {
			return RabbitMqFlows.createFanoutSender(channel, commandExchange, wrapper::wrapSender);		
		}

		@Bean
		public Flowable<ByteBuffer> commandPub(Channel channel, CommunicationWrapper<ByteBuffer> wrapper) throws IOException {
			return RabbitMqFlows.createFanoutReceiver(channel, commandExchange, "common", wrapper::wrapReceiver);		
		}
	}
	
	@Import(CommonContext.class)
	public static class ServerContext {


		@Bean
		public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerServerConnection(Channel channel, CommunicationWrapper<ByteBuffer> wrapper) throws IOException, TimeoutException {
			return RabbitMqFlows.createReplyableFanoutReceiver(channel, commandExchange, "server", wrapper::wrapReceiver);
					//.doOnNext(x -> System.out.println("[STATUS] Received request; " + Arrays.toString(x.getValue().array()) + " replier: " + x.getReplyConsumer()));
		}

		@Bean
		public Service dockerServiceManagerServer(
			//Supplier<? extends DockerServiceBuilder<? extends DockerService>> delegateSupplier,
			@Qualifier("commandChannel") Subscriber<ByteBuffer> commandChannel,
			@Qualifier("commandPub") Flowable<ByteBuffer> commandPublisher,
			@Qualifier("dockerServiceManagerServerConnection") Flowable<SimpleReplyableMessage<ByteBuffer>> requestsFromClients,
			Gson gson
		) throws DockerCertificateException {
	        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();


//		        DefaultDockerClient.builder().s

	        // Bind container port 443 to an automatically allocated available host
	        // port.
	        String[] ports = { "80", "22" };
	        Map<String, List<PortBinding>> portBindings = new HashMap<>();
	        for (String port : ports) {
	            List<PortBinding> hostPorts = new ArrayList<>();
	            hostPorts.add(PortBinding.of("0.0.0.0", port));
	            portBindings.put(port, hostPorts);
	        }

	        List<PortBinding> randomPort = new ArrayList<>();
	        randomPort.add(PortBinding.randomPort("0.0.0.0"));
	        portBindings.put("443", randomPort);

	        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
//	        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder()
//	                .hostConfig(hostConfig);
	        

	        DockerServiceSystem<?> dss = new DockerServiceSystemDockerClient(dockerClient,
	        		() -> ContainerConfig.builder()
	                .hostConfig(hostConfig),
	                true, null);//DockerServiceFactoryDockerClient.create(hostMode, env, networks);
	        

	        // Create a supplier that yields preconfigured builders
	        Supplier<DockerServiceBuilder<? extends DockerService>> builderSupplier = dss::newServiceBuilder;//() -> {
//	        	DockerServiceBuilderDockerClient dockerServiceBuilder = new DockerServiceBuilderDockerClient(
//	        			dockerClient, containerConfigBuilder, true, null);
//
////		        dockerServiceBuilder
////		        		.setDockerClient(dockerClient)
////		        		.setContainerConfigBuilder(containerConfigBuilder);
//		        
//		        return dockerServiceBuilder;
//	        };
	        
	        DockerServiceManagerServerComponent result =
	        		new DockerServiceManagerServerComponent(
	        				builderSupplier,
	        				commandChannel,
	        				commandPublisher,
	        				requestsFromClients,
	        				gson        				
	        				);
	        result.startAsync().awaitRunning();

	        return result;
		}
		
	}
	
	@Import(CommonContext.class)
	public static class ClientContext {
		
		
		@Bean
		public Function<ByteBuffer, CompletableFuture<ByteBuffer>> dockerServiceManagerClientConnection(Channel channel, CommunicationWrapper<ByteBuffer> wrapper) throws IOException, TimeoutException {
			return RabbitMqFlows.createReplyableFanoutSender(channel, commandExchange, "dockerServiceManagerClientComponent", wrapper::wrapSender, x -> Collections.singletonList(x));
		}

		
		@Bean(initMethod="startUp", destroyMethod="shutDown")
		public DockerServiceManagerClientComponent client(
				@Qualifier("commandPub") Flowable<ByteBuffer> commandPublisher,
				@Qualifier("commandChannel") Subscriber<ByteBuffer> commandSender,				
				@Qualifier("dockerServiceManagerClientConnection") Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
				Gson gson

				) {
			DockerServiceManagerClientComponent result =
					new DockerServiceManagerClientComponent(
						commandPublisher,
						commandSender,
						requestToServer,
						gson,
						"no-requester-id-needed",
						"no-default-container-type-needed"
					);
			
			return result;
		}

		@Bean
		public DockerServiceBuilderFactory<?> dockerServiceManagerClient(
				DockerServiceManagerClientComponent client
		) throws Exception {
			
			DockerServiceBuilderFactory<DockerServiceBuilder<DockerService>> result =
					() -> DockerServiceBuilderJsonDelegate.create(client::create);

			return result;
		}
	}

	public static class AppContext {
		@Bean
		public ApplicationRunner appRunner(DockerServiceBuilderFactory<?> clientFactory) {
			return (args) -> {
				
				DockerServiceBuilder<?> client = clientFactory.get();
				//client.setImageName("library/alpine"); 
				client.setImageName("tenforce/virtuoso");
				DockerService service = client.get();
				service.startAsync().awaitRunning();
		
				System.out.println("[STATUS] Service is running: " + service.getContainerId());
				
				//Thread.sleep(600000);
				
				System.out.println("[STATUS] Waiting for termination");
				service.stopAsync().awaitTerminated();
				System.out.println("[STATUS] Terminated");
			};
		}
	}
	
	@Test
	public void testDockerCommunication() {
		
		// NOTE The broker shuts down when the context is closed
		
		SpringApplicationBuilder builder = new SpringApplicationBuilder()
				.properties(new ImmutableMap.Builder<String, Object>()
						.put("hostMode", true)
						.put(Constants.HOBBIT_SESSION_ID_KEY, "testsession" + "." + RabbitMqFlows.idGenerator.get())
						.build())
				.sources(ConfigQpidBroker.class)
				.child(ConfigGson.class, ConfigCommunicationWrapper.class, ConfigRabbitMqConnectionFactory.class)
					.child(ServerContext.class)
					.sibling(ClientContext.class, AppContext.class);

		try(ConfigurableApplicationContext ctx = builder.run()) {
		} catch(Exception e) {
			System.out.println("[STATUS] Exception caught");
			throw new RuntimeException(e);
		}
	}
}
