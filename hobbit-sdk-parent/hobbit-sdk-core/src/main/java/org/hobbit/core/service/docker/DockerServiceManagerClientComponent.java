package org.hobbit.core.service.docker;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.hobbit.core.Commands;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.reactivestreams.Subscriber;

import com.google.common.primitives.Bytes;
import com.google.gson.Gson;

import io.reactivex.Flowable;

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
public class DockerServiceManagerClientComponent
    extends DockerServiceManagerClientComponentBase
{	
    protected String requesterContainerId;
    protected String requestedContainerType;
    
	public DockerServiceManagerClientComponent(
            Flowable<ByteBuffer> commandPublisher,
            Subscriber<ByteBuffer> commandSender,
            Function<ByteBuffer, CompletableFuture<ByteBuffer>> requestToServer,
            Gson gson,
            String requesterContainerId,
            String requestedContainerType
	        ) {
        super(commandPublisher, commandSender, requestToServer, gson);

        this.requesterContainerId = requesterContainerId;
        this.requestedContainerType = requestedContainerType;
    }

    //private static final Logger logger = LoggerFactory.getLogger(DockerServiceManagerClientComponent.class);


    public Entry<ByteBuffer, String> createStartCommand(String imageName, Map<String, String> env) {
        String[] envArr = EnvironmentUtils.mapToList("=", env).toArray(new String[0]);
        StartCommandData msg = new StartCommandData(imageName, requestedContainerType, requesterContainerId, envArr);
        String jsonStr = gson.toJson(msg);


        ByteBuffer buffer = ByteBuffer.wrap(Bytes.concat(
            new byte[]{Commands.DOCKER_CONTAINER_START},
            RabbitMQUtils.writeString(jsonStr)
        ));

        
        Entry<ByteBuffer, String> result = new SimpleEntry<>(buffer, "" + msg);
        return result;
    }

}


