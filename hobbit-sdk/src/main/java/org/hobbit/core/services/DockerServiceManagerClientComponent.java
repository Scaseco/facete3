package org.hobbit.core.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hobbit.core.Commands;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.transfer.Publisher;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;

/**
 * Client component to communicate with a remote {@link DockerServiceManagerComponent}
 *
 * The component is a service itself: When started, the appropriate hooks are registered
 *
 * @author raven Sep 25, 2017
 *
 */
public class DockerServiceManagerClientComponent
    extends AbstractIdleService
{
    protected WritableByteChannel commandChannel;
    protected Publisher<ByteBuffer> commandPublisher;

    protected Publisher<ByteBuffer> responsePublisher;

    // Some method to send the service creation request
    //protected Function<ByteBuffer, CompletableFuture<ByteBuffer>> requester;

    protected Gson gson;


    protected Map<String, Service> idToService = new HashMap<>();

    /**
     * On start up, the stub registers on the command channel and listens for service
     * termination events in order to update the running state of service stubs
     */
    @Override
    protected void startUp() throws Exception {
        commandPublisher.subscribe(this::handleMessage);
    }

    @Override
    protected void shutDown() throws Exception {
        commandPublisher.unsubscribe(this::handleMessage);
    }

    // Listen to service terminated messages
    public void handleMessage(ByteBuffer msg) {
        if(msg.hasRemaining()) {
            byte cmd = msg.get();
            switch(cmd) {
            case Commands.DOCKER_CONTAINER_TERMINATED:
                String serviceId = RabbitMQUtils.readString(msg);
                int exitCode = msg.get();
                handleServiceTermination(serviceId, exitCode);
                break;
            }
        }
    }

    public void handleServiceTermination(String serviceId, int exitCode) {
        Service service = idToService.get(serviceId);

        if(service != null) {
            // FIXME If the remote service failed, we would here incorrectly set the service to STOPPED
            service.stopAsync();
        }

        idToService.remove(service);
    }

    // These are the delegate target methods of the created by DockerServiceSimpleDelegation
    public String startService(String imageName, Map<String, String> env) {

        // Prepare the message for starting a service
        String[] envArr = EnvironmentUtils.mapToList("=", env).toArray(new String[0]);
        StartCommandData msg = new StartCommandData(imageName, "defaultType", "requester?", envArr);
        String jsonStr = gson.toJson(msg);


        ByteBuffer buffer = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(new byte[][]{
            new byte[]{Commands.DOCKER_CONTAINER_START},
            RabbitMQUtils.writeString(jsonStr)
        }));

        // Send out the message
        // FIXME We need a mechanism to tell the receiver to respond on our responsePublisher
        try {
            commandChannel.write(buffer);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }


        // Not sure if this is a completable future or a subscriber
        CompletableFuture<ByteBuffer> response = PublisherUtils.triggerOnMessage(responsePublisher, (x) -> true);

        ByteBuffer responseBuffer;
        try {
            responseBuffer = response.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        String result = RabbitMQUtils.readString(responseBuffer);

        Service service = new DockerServiceSimpleDelegation(imageName, this::startService, this::stopService);

        idToService.put(result, service);

        return result;
    }

    public void stopService(String serviceId) {
        ByteBuffer buffer = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(new byte[][]{
            new byte[]{Commands.DOCKER_CONTAINER_STOP}, RabbitMQUtils.writeString(serviceId)}));

        try {
            commandChannel.write(buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
