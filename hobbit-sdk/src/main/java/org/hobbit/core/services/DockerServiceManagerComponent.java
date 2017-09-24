package org.hobbit.core.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.jena.ext.com.google.common.util.concurrent.MoreExecutors;
import org.hobbit.core.Commands;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.transfer.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;

/**
 * A service listening on a byte channel for requests to create docker containers
 * This manager handles the actual docker instances - i.e. NOT the service stubs
 *
 *
 * @author raven Sep 24, 2017
 *
 */
public class DockerServiceManagerComponent
    extends AbstractIdleService
{
    private static final Logger logger = LoggerFactory.getLogger(DockerServiceManagerComponent.class);


    @Resource(name="commandChannel")
    protected WritableByteChannel commandChannel;

    @Resource(name="commandPub")
    protected Publisher<ByteBuffer> commandPublisher;

    @Autowired
    protected Gson gson;// = new Gson();


    // Delegate to the actual service instance creation
    protected DockerServiceFactory<DockerService> delegate;


    // The services created by this service manager
    protected Map<String, DockerService> runningManagedServices = new LinkedHashMap<>();
    //protected Set<Service> runningManagedServices = Sets.newIdentityHashSet();


    @Override
    protected void startUp() throws Exception {
        commandPublisher.subscribe(this::receiveCommand);
    }


    @Override
    protected void shutDown() throws Exception {
        commandPublisher.unsubscribe(this::receiveCommand);
    }

    public void onStartServiceRequest(String imageName, Map<String, String> env) {
        //Map<String, String> env = n;

        // TODO Ensure thread safety
        delegate.setImageName(imageName);
        delegate.setLocalEnvironment(env);

        DockerService service = delegate.get();



        service.addListener(new Listener() {
            @Override
            public void running() {
                String containerId = service.getContainerId();

                runningManagedServices.put(containerId, service);
            }

            @Override
            public void terminated(State from) {
                String containerId = service.getContainerId();


                runningManagedServices.remove(containerId);

                // Create the termination message and send it out

                RabbitMQUtils.writeString(containerId);
                int exitCode = service.getExitCode();

                ByteBuffer buffer = ByteBuffer.wrap(RabbitMQUtils.writeByteArrays(new byte[][]{
                        new byte[]{Commands.DOCKER_CONTAINER_TERMINATED},
                        RabbitMQUtils.writeString(containerId),
                        new byte[]{(byte)exitCode}
                }));

                try {
                    commandChannel.write(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                super.terminated(from);
            }

        }, MoreExecutors.directExecutor());


        service.startAsync();
        service.awaitRunning();

    }

    public void onStopServiceRequest(String containerId) {
        Service service = runningManagedServices.get(containerId);
        if(service != null) {
            service.stopAsync();
        } else {
            logger.warn("Stop request ignored due to no running service know by id " + containerId);
        }
    }

    public void receiveCommand(ByteBuffer buffer) {
        if(buffer.hasRemaining()) {
            byte b = buffer.get();
            switch(b) {
            case Commands.DOCKER_CONTAINER_START:
                //parseJson().get("foo")

                String str = RabbitMQUtils.readString(buffer);
                StartCommandData data = gson.fromJson(str, StartCommandData.class);

                String imageName = data.getImage();
                String[] rawEnv = data.getEnvironmentVariables();
                Map<String, String> env = EnvironmentUtils.listToMap("=", Arrays.asList(rawEnv));
                onStartServiceRequest(imageName, env);

//                byte data[] = RabbitMQUtils.writeString(
//                        gson.toJson(new StartCommandData(imageName, "defaultContainerType", "defaultContainerName", EnvironmentU)));


                break;
            case Commands.DOCKER_CONTAINER_STOP:
                String containerId = RabbitMQUtils.readString(buffer);
                onStopServiceRequest(containerId);
                break;
            }
        }
    }

}

