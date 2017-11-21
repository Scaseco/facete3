package org.hobbit.core.service.docker;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.ext.com.google.common.util.concurrent.MoreExecutors;
import org.hobbit.core.Commands;
import org.hobbit.core.config.SimpleReplyableMessage;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.data.StopCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.gson.Gson;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;

/**
 * A service listening on a byte channel for requests to create docker containers
 * This manager handles the actual docker instances - i.e. NOT the service stubs
 *
 *
 * @author raven Sep 24, 2017
 *
 */
public class DockerServiceManagerServerComponent
    extends AbstractIdleService
{
    private static final Logger logger = LoggerFactory.getLogger(DockerServiceManagerServerComponent.class);


//    @Resource(name="commandChannel")
    protected Subscriber<ByteBuffer> commandChannel;

//    @Resource(name="commandPub")
    protected Flowable<ByteBuffer> commandPublisher;

//    @Resource(name="dockerServiceManagerServerConnection")
    protected Flowable<SimpleReplyableMessage<ByteBuffer>> requestsFromClients;
    
//    @Inject
    protected Gson gson;// = new Gson();


    // Delegate to the actual service instance creation
    protected Supplier<? extends DockerServiceBuilder<? extends DockerService>> builderSupplier;


    // The services created by this service manager
    protected Map<String, DockerService> runningManagedServices = Collections.synchronizedMap(new LinkedHashMap<>());
    //protected Set<Service> runningManagedServices = Sets.newIdentityHashSet();

    
    protected transient Disposable commandPublisherUnsubscribe;
    protected transient Disposable clientRequestsUnsubscribe;

    public DockerServiceManagerServerComponent(
    		Supplier<? extends DockerServiceBuilder<? extends DockerService>> delegateSupplier,
    		Subscriber<ByteBuffer> commandChannel,
    		Flowable<ByteBuffer> commandPublisher,
    		Flowable<SimpleReplyableMessage<ByteBuffer>> requestsFromClients,
    		Gson gson
    ) {
        super();
        this.builderSupplier = delegateSupplier;
        
        this.commandChannel = commandChannel;
        this.commandPublisher = commandPublisher;
        this.requestsFromClients = requestsFromClients;
        this.gson = gson;
    }


    @Override
    protected void startUp() throws Exception {
        commandPublisherUnsubscribe = commandPublisher.subscribe(this::onCommand);
        
        clientRequestsUnsubscribe = requestsFromClients.subscribe(this::onRequest);
    }


    @Override
    protected void shutDown() throws Exception {
    	Optional.ofNullable(commandPublisherUnsubscribe).ifPresent(Disposable::dispose);
    	Optional.ofNullable(clientRequestsUnsubscribe).ifPresent(Disposable::dispose);
        //commandPublisher.unsubscribe(this::receiveCommand);
    }

    
    public static ByteBuffer createTerminationMsg(String containerId, int exitCode) {
        RabbitMQUtils.writeString(containerId);

        ByteBuffer buffer = ByteBuffer.wrap(Bytes.concat(
                new byte[]{Commands.DOCKER_CONTAINER_TERMINATED},
                RabbitMQUtils.writeString(containerId),
                new byte[]{(byte)exitCode}
        ));
        
        return buffer;
    }

    public void onStartServiceRequest(String imageName, Map<String, String> env, Consumer<String> idCallback) {
        //Map<String, String> env = n;

    	DockerServiceBuilder<?> builder = builderSupplier.get();
    	
        // TODO Ensure thread safety
        builder.setImageName(imageName);
        builder.setLocalEnvironment(env);

        
        DockerService service;
    	service = builder.get();



        service.addListener(new Listener() {
            @Override
            public void running() {
                String containerId = service.getContainerId();

                runningManagedServices.put(containerId, service);
                
                idCallback.accept(containerId);
            }

            @Override
            public void failed(State from, Throwable failure) {
            	// If the state was starting, we have not yet sent back a response to the dockerServiceClint
            	if(State.STARTING.equals(from)) {
                	idCallback.accept("fail:" + failure.toString());
            	}
            	
            	doTermination();

            	super.failed(from, failure);
            }
            
            @Override
            public void terminated(State from) {
            	doTermination();
            	
                super.terminated(from);
            }

                        
            public void doTermination() {
                String containerId = service.getContainerId();


                runningManagedServices.remove(containerId);

                // Create the termination message and send it out
                int exitCode = service.getExitCode();
                ByteBuffer buffer = createTerminationMsg(containerId, exitCode);

//                try {
                    commandChannel.onNext(buffer);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }            	
            }
            
        }, MoreExecutors.directExecutor());


        service.startAsync();
        service.awaitRunning();

    }

    public void onStopServiceRequest(String containerId) {
        DockerService service = runningManagedServices.get(containerId);
        if(service != null) {
        	System.out.println("Stopping service: " + service.getImageName() + "; container " + service.getContainerId());
            service.stopAsync().awaitTerminated();
        } else {
            logger.warn("Stop request ignored due to no running service know by id " + containerId);
        }
    }
    
    
    public void onRequest(SimpleReplyableMessage<ByteBuffer> request) {
    	ByteBuffer command = request.getValue();

    	onCommand(command, request::reply);
    }

    public void onCommand(ByteBuffer buffer) {
    	onCommand(buffer, null);
    }
    
    public void onCommand(ByteBuffer buffer, Consumer<ByteBuffer> responseTarget) {
    	buffer = buffer.duplicate();

    	if(buffer.hasRemaining()) {
            byte b = buffer.get();
            switch(b) {
            case Commands.DOCKER_CONTAINER_START: {
            	if(responseTarget == null) {
            		logger.warn("Received a request to start a container, however there was no target for the response; therefore ignoring request");
            		return;
            	}
            	
                String str = readRemainingBytesAsString(buffer, StandardCharsets.UTF_8);
                
                StartCommandData data = gson.fromJson(str, StartCommandData.class);

                String imageName = data.getImage();
                String[] rawEnv = data.getEnvironmentVariables();
                Map<String, String> env = EnvironmentUtils.listToMap("=", Arrays.asList(rawEnv));

                onStartServiceRequest(imageName, env, containerId -> {
                    ByteBuffer msg = ByteBuffer.wrap(RabbitMQUtils.writeString(containerId));
                    responseTarget.accept(msg);
                });

                
//                byte data[] = RabbitMQUtils.writeString(
//                        gson.toJson(new StartCommandData(imageName, "defaultContainerType", "defaultContainerName", EnvironmentU)));


                break; }
            case Commands.DOCKER_CONTAINER_STOP: {
                if(responseTarget == null) {
            		logger.warn("Received a request to start a container, however there was no target for the response; therefore ignoring request");
            		return;
            	}

//                byte data[] = RabbitMQUtils.writeString(gson.toJson(new StopCommandData(containerName)));
                String str = readRemainingBytesAsString(buffer, StandardCharsets.UTF_8);
                
                
            	StopCommandData data = gson.fromJson(str, StopCommandData.class);
            	String containerId = data.getContainerName();
            	try {
            		onStopServiceRequest(containerId);
            	} finally {
	                // Reply to the requester that its requets was executed
	                ByteBuffer response = createTerminationMsg(containerId, -1);
	                
	                responseTarget.accept(response);
            	}

                break; }
            }
        }
    }

	// https://stackoverflow.com/questions/17354891/java-bytebuffer-to-string

    /**
     * This function reads *all* remaining bytes in a byte buffer as a string.
     * @param b
     * @param charset
     * @return
     */
    public static String readRemainingBytesAsString(ByteBuffer b, Charset charset) {
    	String result;
    	if(b.hasArray()) {
    		result = new String(b.array(), b.arrayOffset() + b.position(), b.remaining(), charset);
    	} else {
    		byte[] tmp = new byte[b.remaining()];
    		b.duplicate().get(tmp);
    		result = new String(tmp, charset);
    	}
    	
    	//b.position(b.arrayOffset() + b.position() + result.getBytes(charset).length);
    	b.position(b.limit());
    	
    	return result;
    }
}

