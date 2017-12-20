package org.hobbit.benchmark.faceted_browsing.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceDockerClient;
import org.hobbit.core.service.docker.DockerServiceFactory;
import org.hobbit.core.service.docker.EnvironmentUtils;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;

public class DockerServiceFactoryDockerClient
	implements DockerServiceFactory<DockerService>
{
    protected DockerClient dockerClient;
    protected Supplier<ContainerConfig.Builder> containerConfigBuilderSupplier;
    protected boolean hostMode;
    protected Set<String> networks;

	
	public DockerServiceFactoryDockerClient(
			DockerClient dockerClient,
			Supplier<Builder> containerConfigBuilderSupplier,
			boolean hostMode,
			Set<String> networks
			) {
		super();

		Objects.requireNonNull(dockerClient);
		Objects.requireNonNull(containerConfigBuilderSupplier);
		
		this.dockerClient = dockerClient;
		this.containerConfigBuilderSupplier = containerConfigBuilderSupplier;
		this.hostMode = hostMode;
		this.networks = networks;
	}


	@Override
	public DockerService create(String imageName, Map<String, String> localEnv) {
		Builder builder = containerConfigBuilderSupplier.get();
		Map<String, String> env = new LinkedHashMap<>();
		env.putAll(EnvironmentUtils.listToMap(builder.build().env()));
		env.putAll(localEnv);
		
		ContainerConfig containerConfig = builder
			.image(imageName)
			.env(EnvironmentUtils.mapToList(env))
			.build();
		
		DockerServiceDockerClient result = new DockerServiceDockerClient(dockerClient, containerConfig, hostMode, networks);

		return result;
	}

}
