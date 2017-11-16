package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hobbit.core.Constants;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.config.HobbitConfigChannelsPlatform;
import org.hobbit.core.config.HobbitConfigCommon;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderDockerClient;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.qpid.config.ConfigQpidBroker;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

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
	
	protected Function<ByteBuffer, CompletableFuture<ByteBuffer>> channel;
	
	@Bean
	public String commandExchange() {
		return Constants.HOBBIT_COMMAND_EXCHANGE_NAME;
	}
	
	@Bean
	public Connection connection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
		return connectionFactory.newConnection();
	}
	
	@Bean
	public Flowable<ByteBuffer> commandPub(Connection connection, @Qualifier("commandExchange") String commandExchange) throws IOException {
		return HobbitConfigChannelsPlatform.createFanoutReceiver(connection, commandExchange);
	}
	
	@Bean
	public Subscriber<ByteBuffer> commandChannel(Connection connection, @Qualifier("commandExchange") String commandExchange) throws IOException {
		return HobbitConfigChannelsPlatform.createFanoutSender(connection, commandExchange, null);
	}
	
	@Bean
	public Function<ByteBuffer, CompletableFuture<ByteBuffer>> dockerServiceManagerClientConnection(Connection connection, @Qualifier("commandExchange") String commandExchange) throws IOException {
		return HobbitConfigChannelsPlatform.createReplyableFanoutSender(connection, commandExchange, null);
	}

	@Bean
	public Flowable<SimpleReplyableMessage<ByteBuffer>> dockerServiceManagerServerConnection(Connection connection, @Qualifier("commandExchange") String commandExchange) throws IOException {
		Channel channel = connection.createChannel();
		return HobbitConfigChannelsPlatform.createReplyableFanoutReceiver(channel, commandExchange);
	}

//	@Bean
//	public Service rawDockerServiceManagerServer(@Qualifier("rawDockerServiceManagerServer") Service service) throws DockerCertificateException {
//		service.startAsync().awai
//		return service;
//	}
//	
	@Bean
	public Service dockerServiceManagerServer(
		//Supplier<? extends DockerServiceBuilder<? extends DockerService>> delegateSupplier,
		@Qualifier("commandChannel") Subscriber<ByteBuffer> commandChannel,
		@Qualifier("commandPub") Flowable<ByteBuffer> commandPublisher,
		@Qualifier("dockerServiceManagerServerConnection") Flowable<SimpleReplyableMessage<ByteBuffer>> requestsFromClients,
		Gson gson
	) throws DockerCertificateException {
        DockerClient dockerClient = DefaultDockerClient.fromEnv().build();


//	        DefaultDockerClient.builder().s

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
        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder()
                .hostConfig(hostConfig);

        
        // Create a supplier that yields preconfigured builders
        Supplier<DockerServiceBuilder<? extends DockerService>> builderSupplier = () -> {
        	DockerServiceBuilderDockerClient dockerServiceBuilder = new DockerServiceBuilderDockerClient();

	        dockerServiceBuilder
	        		.setDockerClient(dockerClient)
	        		.setContainerConfigBuilder(containerConfigBuilder);
	        
	        return dockerServiceBuilder;
        };
        
        
        		//.getContainerConfigBuilder().exposedPorts(exposedPorts)
        		
        		//.setImageName("busybox");

        
//        dockerServiceBuilder.setImageName("tenforce/virtuoso");
//        DockerService dockerService = dockerServiceBuilder.get();
//        dockerService.startAsync().awaitRunning();
//        
//        System.out.println("Started: " + dockerService.getContainerId());
        
//	        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder()
//	                .hostConfig(hostConfig);
//	        	    .image("busybox").exposedPorts(ports)
//	        	    .cmd("sh", "-c", "while :; do sleep 1; done")

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
	
	@Bean(initMethod="startUp")
	public DockerServiceBuilder<DockerService> dockerServiceManagerClient(
			@Qualifier("commandPub") Flowable<ByteBuffer> commandPublisher,
			@Qualifier("dockerServiceManagerClientConnection") Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
			Gson gson
	) throws Exception {
		DockerServiceManagerClientComponent result =
				new DockerServiceManagerClientComponent(
						commandPublisher,
						requestToServer,
						gson
				);
		
		result.startUp();
		return result;
	}
	
	
		
	@Test
	public void testDockerCommunication() {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
			.sources(ConfigQpidBroker.class)
			.sources(HobbitConfigCommon.class)
			.sources(ConfigRabbitMqConnectionFactory.class)
			.sources(TestDockerCommunication.class)
			.run();
		
		DockerServiceBuilder<DockerService> client = (DockerServiceBuilder<DockerService>) ctx.getBean("dockerServiceManagerClient");
		client.setImageName("tenforce/virtuoso");
		DockerService service = client.get();
		service.startAsync().awaitRunning();
		
		System.out.println("Service is running");
		
		service.stopAsync();
		
	}
}
