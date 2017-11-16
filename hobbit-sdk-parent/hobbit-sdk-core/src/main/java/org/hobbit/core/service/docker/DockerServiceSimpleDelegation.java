package org.hobbit.core.service.docker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.common.util.concurrent.AbstractIdleService;

public class DockerServiceSimpleDelegation
    extends AbstractIdleService
    implements DockerService
{
    protected String imageName;
    protected Map<String, String> localEnvironment = new LinkedHashMap<>();

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

    @Override
    public String getImageName() {
        return imageName;
    }

    @Override
    public String getContainerId() {
        return containerId;
    }

    @Override
    protected void startUp() throws Exception {
        containerId = startServiceDelegate.apply(imageName, localEnvironment);
    }

    @Override
    protected void shutDown() throws Exception {
        stopServiceDelegate.accept(containerId);
    }

    @Override
    public int getExitCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
