package org.hobbit.core.services;

import java.util.Objects;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;

/**
 * Implementation of DockerServiceFactory for docker containers backed by spotify's docker client
 *
 * @author raven Sep 20, 2017
 *
 */
public class DockerServiceFactoryDockerClient
    implements DockerServiceFactory<DockerServiceDockerClient>
{
    protected DockerClient dockerClient;
    protected ContainerConfig.Builder containerConfigBuilder;

    public DockerServiceFactoryDockerClient() {
        super();
    }

//    public DockerServiceFactoryDockerClient(DockerClient dockerClient) {
//        this(null, null); //new ContainerCon
//    }

    public DockerServiceFactoryDockerClient(DockerClient dockerClient, ContainerConfig.Builder containerConfigBuilder) {
        super();
        this.dockerClient = dockerClient;
        this.containerConfigBuilder = containerConfigBuilder;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public DockerServiceFactoryDockerClient setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        return this;
    }


    public String getImageName() {
        return containerConfigBuilder.build().image();
    }

    public DockerServiceFactoryDockerClient setImageName(String imageName) {
        containerConfigBuilder.image(imageName);
        return this;
    }


    public ContainerConfig.Builder getContainerConfigBuilder() {
        return containerConfigBuilder;
    }

    public DockerServiceFactoryDockerClient setContainerConfigBuilder(ContainerConfig.Builder containerConfigBuilder) {
        this.containerConfigBuilder = containerConfigBuilder;
        return this;
    }

    @Override
    public DockerServiceDockerClient get() {
        Objects.requireNonNull(dockerClient);
        Objects.requireNonNull(containerConfigBuilder);

        ContainerConfig containerConfig = containerConfigBuilder.build();

        DockerServiceDockerClient result = new DockerServiceDockerClient(dockerClient, containerConfig);
        return result;
    }
}
