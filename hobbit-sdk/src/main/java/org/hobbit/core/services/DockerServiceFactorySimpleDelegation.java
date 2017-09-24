package org.hobbit.core.services;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Delegates the creation of a docker container to the provided
 * start / stop and run functions.
 *
 *
 * FIXME How to determine whether the service is running / terminated?
 * - One option could be that there has to be a run function which blocks as long as the remote service is running.
 *   For instance, the run function could wait for the termination signal from the platform
 *   Downside: The service needs an extra thread just to wait
 * - Another option: A handler outside of the service receives the signals and calls stop
 *   This is the way to go
 *
 * @author raven Sep 24, 2017
 *
 */
public class DockerServiceFactorySimpleDelegation
    implements DockerServiceFactory<DockerService>, Cloneable
{
    protected String imageName;
    protected Map<String, String> localEnvironment;

    // Delegate to start a service; arguments are the image Name and the localEnvironment
    // Result must be the container id
    protected BiFunction<String, Map<String, String>, String> startServiceDelegate;

    // Function to stop a container. Argument is the container id
    protected Consumer<String> stopServiceDelegate;

    protected Runnable runDelegate;

    // Registration for state changes of the container
    // protected Function<String, Publisher<Sring>> serviceStatus;


    public DockerServiceFactorySimpleDelegation(
            BiFunction<String, Map<String, String>, String> startServiceDelegate,
            Consumer<String> stopServiceDelegate) {
        super();
        this.startServiceDelegate = startServiceDelegate;
        this.stopServiceDelegate = stopServiceDelegate;
        this.localEnvironment = new HashMap<>();
    }

    @Override
    public DockerServiceFactorySimpleDelegation clone() throws CloneNotSupportedException {
        DockerServiceFactorySimpleDelegation result = new DockerServiceFactorySimpleDelegation(
                startServiceDelegate,
                stopServiceDelegate
        );
        result.setImageName(imageName);
        result.setLocalEnvironment(localEnvironment);
        return result;
    }

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public DockerServiceFactorySimpleDelegation setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    @Override
    public Map<String, String> getLocalEnvironment() {
        return localEnvironment;
    }

    @Override
    public DockerServiceFactorySimpleDelegation setLocalEnvironment(Map<String, String> environment) {
        this.localEnvironment = environment;
        return this;
    }

    @Override
    public DockerService get() {
        DockerServiceSimpleDelegation result = new DockerServiceSimpleDelegation(imageName, startServiceDelegate, stopServiceDelegate);
        return result;
    }


}
