package org.hobbit.core.service.docker;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
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
    protected Set<String> networks;


    // Status fields for running services
    // Container id (requires the service to be running)
    protected String containerId;

    protected String containerName;
    
    protected boolean hostMode;
    
    public DockerServiceDockerClient(DockerClient dockerClient, ContainerConfig containerConfig, boolean hostMode, Set<String> networks) {
        super();
        this.dockerClient = dockerClient;
        this.containerConfig = containerConfig;
        this.hostMode = hostMode;
        this.networks = networks;
    }

    @Override
    protected void startUp() throws Exception {
    	//String imageName = getImageName();
    	//dockerClient.pull(imageName);
    	
        ContainerCreation creation = dockerClient.createContainer(containerConfig);
        containerId = creation.id();

        // Start container
        dockerClient.startContainer(containerId);

        // TODO Make this configurable from the context
        //String HOBBIT_DOCKER_NETWORK = "hobbit";
        for(String network : networks) {
        	dockerClient.connectToNetwork(containerId, network);
        }
        //dockerClient.connectToNetwork(containerId, HOBBIT_DOCKER_NETWORK);

//        gelfAddress = System.getenv(LOGGING_GELF_ADDRESS_KEY);
//        if (gelfAddress == null) {
//            LOGGER.info(
//                    "Didn't find a gelf address ({}). Containers created by this platform will use the default logging.",
//                    LOGGING_GELF_ADDRESS_KEY);
//        }
        // try to find hobbit network in existing ones
//        List<Network> networks = dockerClient.listNetworks();
//        String hobbitNetwork = null;
//        for (Network net : networks) {
//            if (net.name().equals(HOBBIT_DOCKER_NETWORK)) {
//                hobbitNetwork = net.id();
//                break;
//            }
//        }
//        // if not found - create new one
//        if (hobbitNetwork == null) {
//            final NetworkConfig networkConfig = NetworkConfig.builder().name(HOBBIT_DOCKER_NETWORK).build();
//            dockerClient.createNetwork(networkConfig);
//        }
//    }

        
        ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

        if(hostMode) {
        	containerName = containerInfo.networkSettings().ipAddress();
        } else {
        	containerName = containerInfo.name();
        	containerName = containerName.startsWith("/") ? containerName.substring(1) : containerName;
        }
        
        
        
        //NetworkCreation x = dockerClient.createNetwork(NetworkConfig.builder().name(containerName).build());
        //dockerClient.connectToNetwork(containerId, containerName);
        
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
    	try {
    		dockerClient.killContainer(containerId);
    		dockerClient.removeContainer(containerId);
    	} catch(Exception e) {

    		boolean acceptableException = false;
    		// Maybe the underlying container is already stopped or already terminated
    		try {
    			ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
    			boolean isContainerStopped = !containerInfo.state().running();
    			acceptableException = isContainerStopped;
    		} catch(ContainerNotFoundException f) {
    			acceptableException = true;
    		}
    		
    		if(!acceptableException) {
    			throw new RuntimeException(e);
    		}
    		
    	}
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
        //return containerId;
    	return containerName;
    }

    @Override
    protected void runOneIteration() throws Exception {
        ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
        ContainerState containerState = containerInfo.state();
        if(!containerState.running()) {
        	stopAsync();
        }
//        if(!containerState.running()) {
//            throw new IllegalStateException("A docker container that should act as a service is no longer running. Container id = " + containerId);
//        }

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

