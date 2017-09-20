package org.hobbit.core.services;

import java.util.Objects;

import com.google.common.collect.Tables;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.HostConfig;

import junit.framework.Assert;

/**
 * Implementation of DockerServiceFactory for docker containers backed by spotify's docker client
 *
 * @author raven Sep 20, 2017
 *
 */
public class DockerServiceFactoryDockerClient
    extends DockerServiceFactoryBase<DockerServiceDockerClient>
{
    protected DockerClient dockerClient;
    protected HostConfig hostConfig;


    protected String imageName;

    public DockerServiceFactoryDockerClient() {
        super();
    }

    public DockerServiceFactoryDockerClient(DockerClient dockerClient) {
        super();
        this.dockerClient = dockerClient;
    }

    public DockerServiceFactoryDockerClient(DockerClient dockerClient, HostConfig hostConfig) {
        super();
        this.dockerClient = dockerClient;
        this.hostConfig = hostConfig;
    }

    public DockerServiceFactoryDockerClient setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public DockerServiceFactoryDockerClient setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
        return this;
    }

    public HostConfig getHostConfig() {
        return hostConfig;
    }

    public DockerServiceFactoryDockerClient setHostConfig(HostConfig hostConfig) {
        this.hostConfig = hostConfig;
        return this;
    }

    public String getImageName() {
        return imageName;
    }

    @Override
    public DockerServiceDockerClient get() {
        Objects.requireNonNull(dockerClient);
        Objects.requireNonNull(hostConfig);
        Objects.requireNonNull(imageName);

        DockerServiceDockerClient result = new DockerServiceDockerClient(dockerClient, hostConfig, imageName);
        return result;
    }
}
