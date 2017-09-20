package org.hobbit.core.services;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class DockerServiceFactorySimpleDelegation
    implements DockerServiceFactory<DockerService>
{
    protected String imageName;
    protected Map<String, String> localEnvironment;

    // Delegate to start a service; arguments are the image Name and the localEnvironment
    // Result must be the container id
    protected BiFunction<String, Map<String, String>, String> startServiceDelegate;

    // Function to stop a container. Argument is the container id
    protected Consumer<String> stopServiceDelegate;


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
