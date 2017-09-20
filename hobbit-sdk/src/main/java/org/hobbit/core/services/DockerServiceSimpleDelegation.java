package org.hobbit.core.services;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.common.util.concurrent.AbstractIdleService;

public class DockerServiceSimpleDelegation
    extends AbstractIdleService
    implements DockerService
{
    protected Map<String, String> localEnvironment;

//    protected WritableByteChannel channel;
//    protected Publisher<ByteBuffer> publisher;

    protected String imageName;

    protected BiFunction<String, Map<String, String>, String> startServiceDelegate;

    // Function to stop a container. Argument is the container id
    protected Consumer<String> stopServiceDelegate;



    protected String containerId;

    public DockerServiceSimpleDelegation(String imageName,
            BiFunction<String, Map<String, String>, String> startServiceDelegate,
            Consumer<String> stopServiceDelegate) {
        this.imageName = imageName;
        this.startServiceDelegate = startServiceDelegate;
        this.stopServiceDelegate = stopServiceDelegate;
    }

//    public DockerServiceSimpleDelegation(Map<String, String> localEnvironment, WritableByteChannel channel,
//            Publisher<ByteBuffer> publisher) {
//        super();
//        this.localEnvironment = localEnvironment;
//        this.channel = channel;
//        this.publisher = publisher;
//    }

    @Override
    protected void startUp() throws Exception {
//        byte data[] = RabbitMQUtils.writeString(
//                gson.toJson(new StartCommandData(imageName, containerType, containerName, envVariables)));
//        //BasicProperties props = new BasicProperties.Builder().deliveryMode(2).replyTo(responseQueueName).build();
//        sendToCmdQueue(Commands.DOCKER_CONTAINER_START, data, props);

        /*
        try {
            envVariables = envVariables != null ? Arrays.copyOf(envVariables, envVariables.length + 2) : new String[2];
            envVariables[envVariables.length - 2] = Constants.RABBIT_MQ_HOST_NAME_KEY + "=" + rabbitMQHostName;
            envVariables[envVariables.length - 1] = Constants.HOBBIT_SESSION_ID_KEY + "=" + getHobbitSessionId();
            initResponseQueue();
            byte data[] = RabbitMQUtils.writeString(
                    gson.toJson(new StartCommandData(imageName, containerType, containerName, envVariables)));
            BasicProperties props = new BasicProperties.Builder().deliveryMode(2).replyTo(responseQueueName).build();
            sendToCmdQueue(Commands.DOCKER_CONTAINER_START, data, props);
            QueueingConsumer.Delivery delivery = responseConsumer.nextDelivery(DEFAULT_CMD_RESPONSE_TIMEOUT);
            Objects.requireNonNull(delivery, "Didn't got a response for a create container message.");
            if (delivery.getBody().length > 0) {
                return RabbitMQUtils.readString(delivery.getBody());
            }
        } catch (Exception e) {
            LOGGER.error("Got exception while trying to request the creation of an instance of the \"" + imageName
                    + "\" image.", e);
        }
        */
    }

    @Override
    protected void shutDown() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public String getContainerId() {
        return containerId;
    }

}
