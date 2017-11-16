package org.hobbit.benchmark.faceted_browsing.main;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.hobbit.core.config.HobbitConfigChannelsPlatform;
import org.hobbit.core.config.HobbitConfigCommon;
import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceBuilder;
import org.hobbit.core.service.docker.DockerServiceBuilderDockerClient;
import org.hobbit.core.service.docker.DockerServiceManagerClientComponent;
import org.hobbit.core.service.docker.DockerServiceManagerServerComponent;
import org.hobbit.qpid.config.ConfigQpidBroker;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.Service;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

public class TestDockerCommunication {
	
	protected Function<ByteBuffer, CompletableFuture<ByteBuffer>> channel;
	
	@Bean
	public Function<ByteBuffer, CompletableFuture<ByteBuffer>> dockerServerConnection() {
	}
	
	@Bean
	public Service dockerServiceManagerServer() throws DockerCertificateException {
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

        DockerServiceBuilderDockerClient dockerServiceBuilder = new DockerServiceBuilderDockerClient();

        dockerServiceBuilder
        		.setDockerClient(dockerClient)
        		.setContainerConfigBuilder(containerConfigBuilder);
        		//.setImageName("busybox");

        
        dockerServiceBuilder.setImageName("tenforce/virtuoso");
        DockerService dockerService = dockerServiceBuilder.get();
        dockerService.startAsync().awaitRunning();
        
        System.out.println("Started: " + dockerService.getContainerId());
        
//	        ContainerConfig.Builder containerConfigBuilder = ContainerConfig.builder()
//	                .hostConfig(hostConfig);
//	        	    .image("busybox").exposedPorts(ports)
//	        	    .cmd("sh", "-c", "while :; do sleep 1; done")

		return new DockerServiceManagerServerComponent(dockerServiceBuilder);
	}
	
	@Bean
	public DockerServiceBuilder<DockerService> dockerServiceManagerClient() {
		return new DockerServiceManagerClientComponent();
	}
	
	
		
	@Test
	public void testDockerCommunication() {
		new SpringApplicationBuilder()
			.sources(ConfigQpidBroker.class)
			.sources(HobbitConfigCommon.class)
			.sources(HobbitConfigChannelsPlatform.class)
			.sources(TestDockerCommunication.class)
			.run();
		
	}
}
