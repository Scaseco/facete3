package org.hobbit.core.service.docker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.Network;

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
    protected String name;
    

    // Status fields for running services
    // Container id (requires the service to be running)
    protected String containerId;
    protected Integer exitCode;
    
    protected String containerName;
    
    protected boolean hostMode;
    
    public DockerServiceDockerClient(DockerClient dockerClient, ContainerConfig containerConfig, String name, boolean hostMode, Set<String> networks) {
        super();
        this.dockerClient = dockerClient;
        this.containerConfig = containerConfig;
        this.name = name;
        this.hostMode = hostMode;
        this.networks = networks;
    }

    @Override
    protected void startUp() throws Exception {    	
    	//String imageName = getImageName();
    	//dockerClient.pull(imageName);
    	logger.info("Attempting to start docker container: " + getImageName() + " env: " + containerConfig.env() + " hostMode: " + hostMode + " networks: " + networks);
    	
        ContainerCreation creation = dockerClient.createContainer(containerConfig, name);
        containerId = creation.id();


        ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);

        // If a list of networks was specified, disconnect from any other networks
        String ip;
//        hostMode = false;
        if(!hostMode && networks != null) {
	        for(String networkName : containerInfo.networkSettings().networks().keySet()) {
	        	//String networkName = dockerClient.inspectNetwork(attachedNetwork.networkId()).name();
	        	logger.info("Disconnecting container " + containerId + " from network " + networkName); //attachedNetwork.networkId() +" [" + networkName + "]");
	        	dockerClient.disconnectFromNetwork(containerId, networkName); //attachedNetwork.networkId()
	        }
	        
//	        Map<String, String> networkNameToId = dockerClient.listNetworks().stream()
//	        		.collect(Collectors.toMap(Network::name, Network::id));
	    	
	        // TODO Make this configurable from the context
	        //String HOBBIT_DOCKER_NETWORK = "hobbit";
	        for(String networkName : networks) {
	        	//String networkId = networkNameToId.get(network);
	        	logger.info("Connecting container " + containerId + " to network " + networkName); // + " [" + networkId + "]");
	        	dockerClient.connectToNetwork(containerId, networkName);
	        }

//	        containerInfo = dockerClient.inspectContainer(containerId);
	        
	        //String networkId = networkNameToId.get(Iterables.getFirst(networks, null));
//	        String networkName = Iterables.getFirst(networks, null);
//        	ip = containerInfo.networkSettings().networks().get(networkName).ipAddress();
        } else {
//        	ip = containerInfo.networkSettings().ipAddress();
        }
        
        // Start container
        dockerClient.startContainer(containerId);

        containerInfo = dockerClient.inspectContainer(containerId);

//        if(!hostMode && networks != null) {
//	        for(AttachedNetwork attachedNetwork : containerInfo.networkSettings().networks().values()) {
//	        	String networkName = dockerClient.inspectNetwork(attachedNetwork.networkId()).name();
//	        	if(!networks.contains(networkName)) {
//	        		logger.info("Disconnecting container " + containerId + " from network " + attachedNetwork.networkId() +" [" + networkName + "]");
//	        		dockerClient.disconnectFromNetwork(containerId, attachedNetwork.networkId());
//	        	}
//	        }
//        }
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

        
        containerInfo = dockerClient.inspectContainer(containerId);

        if(hostMode) {
        	ip = containerInfo.networkSettings().ipAddress();

        	containerName = ip;
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
    		// The underlying container is already stopped or already terminated
    		// In case the container does no longer exist, we omit further exceptions (as startUp and run should have already thrown them)
    		try {
    			ContainerInfo containerInfo = dockerClient.inspectContainer(containerId);
    			boolean isContainerStopped = !containerInfo.state().running();
    			acceptableException = isContainerStopped;
    			exitCode = containerInfo.state().exitCode();
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
    public Integer getExitCode() {
        //logger.warn("STUB! Exist code always returns 0");
        return exitCode;
    }

}

