package org.hobbit.core.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

import org.hobbit.core.Commands;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.transfer.Publisher;

import com.google.common.util.concurrent.AbstractIdleService;
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

    protected Gson gson;


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
                handleServiceTermination(serviceId);
                break;
            }
        }
    }

    public void handleServiceTermination(String serviceId) {

    }

    // These are the delegate target methods of the created by DockerServiceSimpleDelegation
    public String startService(String imageName, Map<String, String> env) throws IOException {

        // Prepare the message for starting a service
        String[] envArr = EnvironmentUtils.mapToList("=", env).toArray(new String[0]);
        StartCommandData msg = new StartCommandData(imageName, "defaultType", "requester?", envArr);
        String jsonStr = gson.toJson(msg);


        ByteBuffer buffer = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(new byte[][]{
            new byte[]{Commands.DOCKER_CONTAINER_START},
            RabbitMQUtils.writeString(jsonStr)
        }));

        // Send out the message
        commandChannel.write(buffer);

        // FIXME We now need to get a response for the service creation request!

        PublisherUtils.triggerOnMessage(commandPublisher,
                ByteChannelUtils.firstByteEquals(Commands.DOCKER_CONTAINER_STOP));

        // The response is the serviceId
        // FIXME The issue is, that we now need a response specifically to this request
        String result = null;//;
        return result;
    }

    public void stopService(String serviceId) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(new byte[][]{
            new byte[]{Commands.DOCKER_CONTAINER_STOP}, RabbitMQUtils.writeString(serviceId)}));

        commandChannel.write(buffer);
    }

}
