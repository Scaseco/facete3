package org.hobbit.core.services;

import com.google.common.util.concurrent.AbstractIdleService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.HostConfig;

/**
 * A DockerService backed by spotify's docker client
 *
 * Service for
 *
 *
 * @author raven Sep 20, 2017
 *
 */
public class DockerServiceDockerClient
    extends AbstractIdleService
    implements DockerService
{
    protected DockerClient dockerClient;
    protected ContainerConfig containerConfig;


    // Status fields for running services

    // Container id (requires the service to be running)
    protected String containerId;


    protected String execOutput;


    public DockerServiceDockerClient(DockerClient dockerClient, ContainerConfig containerConfig) {
        super();
        this.dockerClient = dockerClient;
        this.containerConfig = containerConfig;
    }

    @Override
    protected void startUp() throws Exception {


        ContainerCreation creation = dockerClient.createContainer(containerConfig);
        String id = creation.id();

        // Start container
        dockerClient.startContainer(id);

        // Exec command inside running container with attached STDOUT and STDERR
//        String[] command = null; //{"bash", "-c", "ls"};
//        ExecCreation execCreation = dockerClient.execCreate(
//            id, command, DockerClient.ExecCreateParam.attachStdout(),
//            DockerClient.ExecCreateParam.attachStderr());
//        LogStream output = dockerClient.execStart(execCreation.id());
//        execOutput = output.readFully();
    }

    @Override
    protected void shutDown() throws Exception {
        // Kill container
        dockerClient.killContainer(containerId);

        // Remove container
        dockerClient.removeContainer(containerId);

        // Close the docker client
        //docker.close();
    }

    @Override
    public String getImageName() {
        String result = containerConfig.image();
        return result;
    }

    @Override
    public String getContainerId() {
        return containerId;
    }

}

