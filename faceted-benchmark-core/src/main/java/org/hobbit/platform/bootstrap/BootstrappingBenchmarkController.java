package org.hobbit.platform.bootstrap;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.function.Supplier;

import org.hobbit.benchmarks.faceted_browsing.components.CountingSupplier;
import org.hobbit.benchmarks.faceted_browsing.components.PseudoHobbitPlatformController;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.core.services.DockerService;
import org.hobbit.core.services.DockerServiceFactory;
import org.hobbit.core.services.DockerServiceFactorySimpleDelegation;
import org.hobbit.core.services.EnvironmentUtils;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.transfer.PublishingWritableByteChannel;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;

import com.google.common.util.concurrent.Service;



public class BootstrappingBenchmarkController
    extends AbstractBenchmarkController
{
    protected PublishingWritableByteChannel commandPublisher;

//    public static void consumerAsChannel

    @Override
    public void init() throws Exception {
        super.init();

        // Set up configuration environment

        WritableByteChannel commandChannel = ByteChannelUtils.wrapConsumer(
                (buffer) -> PseudoHobbitPlatformController.forwardToHobbit(buffer, (t, u) -> {
                    try {
                        sendToCmdQueue(t, u);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

        commandPublisher = new PublishingWritableByteChannelSimple();

        DockerServiceFactorySimpleDelegation dockerServiceFactory =
                new DockerServiceFactorySimpleDelegation(
                    (imageName, env) -> createContainer(
                            imageName, EnvironmentUtils.mapToList("=", env).toArray(new String[0])),
                    (containerId) -> stopContainer(containerId)
                    );

        //dockerServiceFactory.clone().setLocalEnvironment();

        // TODO I think the task service factory has to start the docker container and in addition wait on
        // the ready signal

        // TODO Different (docker)serviceFactories should have their own environments

        Supplier<Service> taskServiceFactory = CountingSupplier.from(count -> {
                    dockerServiceFactory.getLocalEnvironment().put(Constants.GENERATOR_ID_KEY, "" + count);
                    return dockerServiceFactory.get();
                });




//        this.createTaskGenerators(taskGeneratorImageName, numberOfTaskGenerators, envVariables);

    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        try {
            commandPublisher.write(PseudoHobbitPlatformController.toByteBuffer(command, data));
        } catch (IOException e) {
            throw new RuntimeException();
        }

        super.receiveCommand(command, data);
    }

    @Override
    protected void executeBenchmark() throws Exception {



    }
}
