package org.hobbit.core.services;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;

/**
 * A DockerService backed by spotify's docker client
 *
 *
 * TODO Make scheduler for polling health status configurable, at present it checks every 10 seconds
 *
 * @author raven Sep 20, 2017
 *
 */
public class DockerServiceDockerClient
    extends AbstractScheduledService
    implements DockerService
{

    private static final Logger logger = LoggerFactory.getLogger(DockerServiceDockerClient.class);


    protected DockerClient dockerClient;
    protected ContainerConfig containerConfig;


    // Status fields for running services
    // Container id (requires the service to be running)
    protected String containerId;

    public DockerServiceDockerClient(DockerClient dockerClient, ContainerConfig containerConfig) {
        super();
        this.dockerClient = dockerClient;
        this.containerConfig = containerConfig;
    }

    @Override
    protected void startUp() throws Exception {
        ContainerCreation creation = dockerClient.createContainer(containerConfig);
        containerId = creation.id();

        // Start container
        dockerClient.startContainer(containerId);

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
        dockerClient.killContainer(containerId);
        dockerClient.removeContainer(containerId);

        // Closing the docker client has to be done elsewhere
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

    @Override
    protected void runOneIteration() throws Exception {
        ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
        ContainerState containerState = containerInfo.state();
        if(!containerState.running()) {
            throw new IllegalStateException("A docker container that should act as a service is no longer running. Container id = " + containerId);
        }
    }

    @Override
    protected Scheduler scheduler() {
        Scheduler result = Scheduler.newFixedRateSchedule(10, 10, TimeUnit.SECONDS);
        //Scheduler result = Scheduler.newFixedRateSchedule(1, 1, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public int getExitCode() {
        logger.warn("STUB! Exist code always returns 0");
        return 0;
    }

}

