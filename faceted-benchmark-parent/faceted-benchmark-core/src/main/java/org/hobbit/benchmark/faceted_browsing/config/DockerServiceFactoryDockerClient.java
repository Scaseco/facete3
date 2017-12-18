package org.hobbit.benchmark.faceted_browsing.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

	
	public DockerServiceFactoryDockerClient(
			DockerClient dockerClient,
			Supplier<Builder> containerConfigBuilderSupplier,
			boolean hostMode
			) {
		super();

		Objects.requireNonNull(dockerClient);
		Objects.requireNonNull(containerConfigBuilderSupplier);
		
		this.dockerClient = dockerClient;
		this.containerConfigBuilderSupplier = containerConfigBuilderSupplier;
		this.hostMode = hostMode;
	}


	@Override
	public DockerService create(String imageName, Map<String, String> env) {
        List<String> envList = EnvironmentUtils.mapToList("=", env);

		ContainerConfig containerConfig = containerConfigBuilderSupplier.get()
			.image(imageName)
			.env(envList)
			.build();
		
		DockerServiceDockerClient result = new DockerServiceDockerClient(dockerClient, containerConfig, hostMode);

		return result;
	}

}
