package org.hobbit.core.service.docker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hobbit.core.Commands;
import org.hobbit.core.data.StopCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Client component to communicate with a remote {@link DockerServiceManagerServerComponent}
 *
 * The component is a service itself:
 * When started, the appropriate subscriptions are made to the flows - and of course unsubscribed when stopping
 *
 *
 * NOTE Alternative implementation would be: the component is registered as a listener on the event queue;
 * 
 * @author raven Sep 25, 2017
 *
 */
public abstract class DockerServiceManagerClientComponentBase
    extends AbstractIdleService
    //implements IdleServiceCapable, DockerServiceFactory<DockerService> // DockerServiceBuilder<DockerService>
    implements DockerServiceFactory<DockerService>
{	
	private static final Logger logger = LoggerFactory.getLogger(DockerServiceManagerClientComponentBase.class);

	
	// TODO: Probably change ByteBuffer to Object here, and encode the objects
	// internally
	
	protected Flowable<ByteBuffer> commandPublisher;
	protected Subscriber<ByteBuffer> commandSender;
	protected Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer;
	
	
	
	public DockerServiceManagerClientComponentBase(
			Flowable<ByteBuffer> commandPublisher,
			Subscriber<ByteBuffer> commandSender,
			Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
			Gson gson) {
		super();
		this.commandPublisher = commandPublisher;
		this.commandSender = commandSender;
		this.requestToServer = requestToServer;
		this.gson = gson;
	}

    protected Gson gson;
	
    // The set of running service stubs created via this manager
    protected Map<String, DockerServiceSimpleDelegation> runningManagedServiceStubs = Collections.synchronizedMap(new HashMap<>());
    
    // Map used to suppress sending out stop requests to already remotely terminated services
    protected Map<String, Service> terminatedManagedServices = Collections.synchronizedMap(new HashMap<>());


    //protected String imageName;
    //protected Map<String, String> env;


    protected transient Disposable commandPublisherUnsubscribe = null;

    
    /**
     * Method which must return the byte buffer for the start command as part of the result.
     * The second argument of the pair should be a human readable description of the request and its parameters for logging purposes
     * 
     * 
     * @param imageName
     * @param env
     * @return
     */
    protected abstract Entry<ByteBuffer, String> createStartCommand(String imageName, Map<String, String> env);

//    @Override
//    public DockerServiceManagerClientComponent setImageName(String imageName) {
//        this.imageName = imageName;
//        return this;
//    }
//
//    @Override
//    public String getImageName() {
//        return imageName;
//    }
//
//    @Override
//    public DockerServiceManagerClientComponent setLocalEnvironment(Map<String, String> env) {
//        this.env = env;
//        return this;
//    }
//
//    @Override
//    public Map<String, String> getLocalEnvironment() {
//        return env;
//    }




    //public DockerService get() {
    @Override
    public DockerService create(String imageName, Map<String, String> env) {
        Objects.requireNonNull(imageName);

        DockerServiceSimpleDelegation service = new DockerServiceSimpleDelegation(imageName, env, this::startService, this::stopService);

        service.addListener(new Listener() {
            @Override
            public void running() {
                String serviceId = service.getContainerId();
                runningManagedServiceStubs.put(serviceId, service);
            }

            @Override
            public void failed(State from, Throwable failure) {
                String serviceId = service.getContainerId();
                runningManagedServiceStubs.remove(serviceId);
//            	super.failed(from, failure);
            }

            @Override
            public void terminated(State from) {
                String serviceId = service.getContainerId();
                runningManagedServiceStubs.remove(serviceId);
                //terminatedManagedServices.put(serviceId, service);
            }
        }, MoreExecutors.directExecutor());

        return service;
    }


    /**
     * On start up, the stub registers on the command channel and listens for service
     * termination events in order to update the running state of service stubs
     */
    @Override
    public void startUp() throws Exception {
    	logger.info("DockerServiceManagerClientComponent::startUp() invoked");
        commandPublisherUnsubscribe = commandPublisher.subscribe(this::handleMessage);
    }

    @Override
    public void shutDown() throws Exception {
    	logger.info("DockerServiceManagerClientComponent::shutDown() invoked with " + runningManagedServiceStubs.size() + " services still running");
    	Optional.ofNullable(commandPublisherUnsubscribe).ifPresent(Disposable::dispose);
    }

    // Listen to service terminated messages
    public void handleMessage(ByteBuffer msg) {
    	msg = msg.duplicate();

        if(msg.hasRemaining()) {
            byte cmd = msg.get();
            switch(cmd) {
            case Commands.DOCKER_CONTAINER_TERMINATED:
            	msg.limit(msg.limit() - 1);
                String serviceId = DockerServiceManagerServerComponent.readRemainingBytesAsString(msg, StandardCharsets.UTF_8);
            	msg.limit(msg.limit() + 1);

            	int exitCode = msg.get();
                handleServiceTermination(serviceId, exitCode);
                break;
            }
        }
    }

    public void handleServiceTermination(String serviceId, int exitCode) {
    	DockerServiceSimpleDelegation serviceStub = runningManagedServiceStubs.get(serviceId);

        if(serviceStub != null) {
            logger.info("Received termination message for " + serviceId + " with exit code " + exitCode + " and going to stop local stub " + serviceStub);

            synchronized (serviceStub) {
	        	//runningManagedServices.remove(serviceId);
	        	terminatedManagedServices.put(serviceId, serviceStub);
	        	serviceStub.setExitCode(exitCode);
	        	if(exitCode != 0) {
//	        		try {
	        			serviceStub.declareFailure(new RuntimeException("Remote service terminated with non-zero exit code"));
//	        		} catch(Exception e) {
//	        			// Ensure we do not raise an exception when handling a remote termination message
//	        			e.printStackTrace();
//	        		}
	        	} else {
		        	serviceStub.stopAsync();	        		
	        	}
        	}
        } else {
            logger.info("Ignoring termination message for " + serviceId + " with exit code " + exitCode + " because no local stub exists");
        }

        //idToService.remove(service);
    }
    
    
    // These are the delegate target methods of the created by DockerServiceSimpleDelegation
    public synchronized String startService(String imageName, Map<String, String> env) {

        // Prepare the message for starting a service
        Entry<ByteBuffer, String> startData = createStartCommand(imageName, env);
        ByteBuffer buffer = startData.getKey();
        String msg = startData.getValue();
        
        // Send out the message
        // FIXME We need a mechanism to tell the receiver to respond on our responsePublisher
//        try {
//            commandChannel.onNext(buffer);
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//        }


        // Not sure if this is a completable future or a subscriber
        //CompletableFuture<ByteBuffer> response = PublisherUtils.triggerOnMessage(responsePublisher, (x) -> true);

        
        // IMPORTANT The response should come on a separate queue separate from the commandPub

        logger.info("Sending request to start container from image '" + imageName + "' with env " + env + " and waiting for response");
        
//        if(imageName.equals("git.project-hobbit.eu:4567/gkatsibras/facetedtaskgenerator/image")) {
//        	System.out.println("FOOBARITIS");
//        	new RuntimeException("ARGH").printStackTrace();
//        }
        
        CompletableFuture<ByteBuffer> response = requestToServer.apply(buffer);
        ByteBuffer responseBuffer;
        try {
            responseBuffer = response.get(60, TimeUnit.SECONDS).duplicate();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        String result = DockerServiceManagerServerComponent.readRemainingBytesAsString(responseBuffer, StandardCharsets.UTF_8);;
        
        logger.info("Client received response with docker id: [" + result + "]");
        
        if(result.startsWith("fail:")) {
        	throw new RuntimeException("Failed to start image [" + imageName + "] - server returned: " + result);
        }

        if(result.isEmpty()) {
        	throw new RuntimeException("DockerServiceManager client received a response with an invalid (empty) Id upon launching " + msg);
        }
        
        return result;
    }

    public synchronized void stopService(String serviceId) {
    	//this.commandPublisher

    	// If the service has already terminated, suppress sending the stop request
    	Service service = terminatedManagedServices.get(serviceId);
    	if(service != null) {
    		logger.info("Shutdown of local client for " + serviceId + " complete");
    		terminatedManagedServices.remove(serviceId);
    	} else {
	    	service = runningManagedServiceStubs.get(serviceId);
	    	Objects.requireNonNull(service);
	    	
	    	logger.info("DockerServiceManagerClientComponent::stopService() invoked for id " + serviceId);
	        StopCommandData msg = new StopCommandData(serviceId);
	        String jsonStr = gson.toJson(msg);
	
	        ByteBuffer buffer = ByteBuffer.wrap(Bytes.concat(
	            new byte[]{Commands.DOCKER_CONTAINER_STOP},
	            RabbitMQUtils.writeString(jsonStr)));
	
	//        try {
	        
	        //CompletableFuture<ByteBuffer> responseFuture = requestToServer.apply(buffer);
	        commandSender.onNext(buffer);
	        
//	        try {
//		        //service.awaitTerminated(60, TimeUnit.SECONDS);
//				//responseFuture.get(60, TimeUnit.SECONDS);
//				logger.info("DockerServiceManagerClientstopService() stop confirmation received for id " + serviceId);
//			//} catch (InterruptedException | ExecutionException | TimeoutException e) {
//	        } catch(TimeoutException e) {
//				logger.error("Exception encountered waiting for stop response from container with id " + serviceId, e);
//				throw new RuntimeException(e);
//			}
    	}
            //commandChannel.onNext(buffer);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

}


