package org.hobbit.core.service.docker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;

/**
 * Implementation of DockerServiceFactory for docker containers backed by spotify's docker client
 *
 * @author raven Sep 20, 2017
 *
 */
@Deprecated
public class DockerServiceBuilderDockerClient
    implements DockerServiceBuilder<DockerServiceDockerClient>
{
    protected DockerClient dockerClient;
    protected ContainerConfig.Builder containerConfigBuilder;
    protected boolean hostMode;
    protected Set<String> networks;

    protected Map<String, String> localEnv = new LinkedHashMap<>();
    
//    public DockerServiceBuilderDockerClient() {
//        super();
//    }

//    public DockerServiceFactoryDockerClient(DockerClient dockerClient) {
//        this(null, null); //new ContainerCon
//    }

    public DockerServiceBuilderDockerClient(DockerClient dockerClient, ContainerConfig.Builder containerConfigBuilder, boolean hostMode, Set<String> networks) {
        super();
        this.dockerClient = dockerClient;
        this.containerConfigBuilder = containerConfigBuilder;
        this.hostMode = hostMode;
        this.networks = networks;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public DockerServiceBuilderDockerClient setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        return this;
    }

    public ContainerConfig.Builder getContainerConfigBuilder() {
        return containerConfigBuilder;
    }

    public DockerServiceBuilderDockerClient setContainerConfigBuilder(ContainerConfig.Builder containerConfigBuilder) {
        this.containerConfigBuilder = containerConfigBuilder;
        return this;
    }


    public String getImageName() {
        return containerConfigBuilder.build().image();
    }

    public DockerServiceBuilderDockerClient setImageName(String imageName) {
        containerConfigBuilder.image(imageName);
        return this;
    }


    @Override
    public Map<String, String> getLocalEnvironment() {
    	return localEnv;
//        List<String> env = containerConfigBuilder.build().env();
//        if(env == null) {
//            env = Collections.emptyList();
//        }
//
//        Map<String, String> result = EnvironmentUtils.listToMap("=", env);
//
//        return result;
    }

    @Override
    public DockerServiceBuilder<DockerServiceDockerClient> setLocalEnvironment(Map<String, String> environment) {
    	this.localEnv = environment;
    	return this;
//        List<String> env = EnvironmentUtils.mapToList("=", environment);
//
//        containerConfigBuilder.env(env);
//        return this;
    }


    @Override
    public DockerServiceDockerClient get() {
        Objects.requireNonNull(dockerClient);
        Objects.requireNonNull(containerConfigBuilder);

        // Merge the local environment into that of the containerConfig
        List<String> rawEnv = containerConfigBuilder.build().env();
        if(rawEnv == null) {
        	rawEnv = Collections.emptyList();
        }

        Map<String, String> env = EnvironmentUtils.listToMap(rawEnv);
        env.putAll(localEnv);
        
        List<String> envList = EnvironmentUtils.mapToList(env);
        containerConfigBuilder.env(envList);
        ContainerConfig containerConfig = containerConfigBuilder.build();

        DockerServiceDockerClient result = new DockerServiceDockerClient(dockerClient, containerConfig, null, hostMode, networks);
        return result;
    }
}
