package org.hobbit.core.service.docker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;

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

    public ContainerConfig.Builder getContainerConfigBuilder() {
        return containerConfigBuilder;
    }

    public DockerServiceFactoryDockerClient setContainerConfigBuilder(ContainerConfig.Builder containerConfigBuilder) {
        this.containerConfigBuilder = containerConfigBuilder;
        return this;
    }


    public String getImageName() {
        return containerConfigBuilder.build().image();
    }

    public DockerServiceFactoryDockerClient setImageName(String imageName) {
        containerConfigBuilder.image(imageName);
        return this;
    }


    @Override
    public Map<String, String> getLocalEnvironment() {
        List<String> env = containerConfigBuilder.build().env();
        if(env == null) {
            env = Collections.emptyList();
        }

        Map<String, String> result = EnvironmentUtils.listToMap("=", env);

        return result;
    }

    @Override
    public DockerServiceFactory<DockerServiceDockerClient> setLocalEnvironment(Map<String, String> environment) {
        List<String> env = EnvironmentUtils.mapToList("=", environment);

        containerConfigBuilder.env(env);
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
