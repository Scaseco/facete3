package org.hobbit.core.service.docker;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.hobbit.core.Commands;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.data.StopCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.IdleServiceCapable;

import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.Listener;
import com.google.common.util.concurrent.Service.State;
import com.google.gson.Gson;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * Client component to communicate with a remote {@link DockerServiceManagerServerComponent}
 *
 * The component is a service itself:
 * When started, the appropriate subscriptions are made to the flows - and of course unsubscribed when stopping
 *
 * @author raven Sep 25, 2017
 *
 */
public class DockerServiceManagerClientComponent
    //extends AbstractIdleService
    implements IdleServiceCapable, DockerServiceBuilder<DockerService>
{
	// TODO: Probably change ByteBuffer to Object here, and encode the objects
	// internally
	
	protected Flowable<ByteBuffer> commandPublisher;
	protected Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer;
	
	
	public DockerServiceManagerClientComponent(
			Flowable<ByteBuffer> commandPublisher,
			Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
			Gson gson) {
		super();
		this.commandPublisher = commandPublisher;
		this.requestToServer = requestToServer;
		this.gson = gson;
	}

    protected Gson gson;
	
    protected Map<String, Service> runningManagedServices = new HashMap<>();


    protected String imageName;
    protected Map<String, String> env;


    protected transient Disposable commandPublisherUnsubscribe = null;

    @Override
    public DockerServiceManagerClientComponent setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public DockerServiceManagerClientComponent setLocalEnvironment(Map<String, String> env) {
        this.env = env;
        return this;
    }

    @Override
    public Map<String, String> getLocalEnvironment() {
        return env;
    }




    public DockerService get() {
        Objects.requireNonNull(imageName);

        DockerService service = new DockerServiceSimpleDelegation(imageName, this::startService, this::stopService);

        service.addListener(new Listener() {
            @Override
            public void running() {
                String serviceId = service.getContainerId();
                runningManagedServices.put(serviceId, service);
            }

            @Override
            public void terminated(State from) {
                String serviceId = service.getContainerId();
                runningManagedServices.remove(serviceId);
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
        commandPublisherUnsubscribe = commandPublisher.subscribe(this::handleMessage);
    }

    @Override
    public void shutDown() throws Exception {
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
        Service service = runningManagedServices.get(serviceId);

        if(service != null) {
            // FIXME If the remote service failed, we would here incorrectly set the service to STOPPED
        	synchronized (service) {
	        	service.stopAsync();
	        	runningManagedServices.remove(serviceId);
        	}
        }

        //idToService.remove(service);
    }

    // These are the delegate target methods of the created by DockerServiceSimpleDelegation
    public String startService(String imageName, Map<String, String> env) {

        // Prepare the message for starting a service
        String[] envArr = EnvironmentUtils.mapToList("=", env).toArray(new String[0]);
        StartCommandData msg = new StartCommandData(imageName, "defaultType", "requester?", envArr);
        String jsonStr = gson.toJson(msg);


        ByteBuffer buffer = ByteBuffer.wrap(Bytes.concat(
            new byte[]{Commands.DOCKER_CONTAINER_START},
            RabbitMQUtils.writeString(jsonStr)
        ));

        // Send out the message
        // FIXME We need a mechanism to tell the receiver to respond on our responsePublisher
//        try {
//            commandChannel.onNext(buffer);
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//        }


        // Not sure if this is a completable future or a subscriber
        //CompletableFuture<ByteBuffer> response = PublisherUtils.triggerOnMessage(responsePublisher, (x) -> true);

            
        CompletableFuture<ByteBuffer> response = requestToServer.apply(buffer);
        ByteBuffer responseBuffer;
        try {
            responseBuffer = response.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        String result = DockerServiceManagerServerComponent.readRemainingBytesAsString(responseBuffer, StandardCharsets.UTF_8);;

        System.out.println("Client received response with docker id: " + result);
        
        if(result.startsWith("fail:")) {
        	throw new RuntimeException("Failed to start image [" + imageName + "] - server returned: " + result);
        }
        
        return result;
    }

    public void stopService(String serviceId) {
    	//this.commandPublisher
    	
        StopCommandData msg = new StopCommandData(serviceId);
        String jsonStr = gson.toJson(msg);

        ByteBuffer buffer = ByteBuffer.wrap(Bytes.concat(
            new byte[]{Commands.DOCKER_CONTAINER_STOP},
            RabbitMQUtils.writeString(jsonStr)));

//        try {
        CompletableFuture<ByteBuffer> responseFuture = requestToServer.apply(buffer);
        
        try {
			responseFuture.get(60, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}
        
            //commandChannel.onNext(buffer);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

}


