package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.List;
import java.util.stream.Collectors;

import org.hobbit.core.components.ContainerStateObserver;
import org.hobbit.core.components.PlatformConnector;

public class ServiceFactoryViaPlatformConnector
    implements ServiceFactory
{
    protected PlatformConnector connector;

    public ServiceFactoryViaPlatformConnector(PlatformConnector connector) {
        super();
        this.connector = connector;
    }

    public ServiceClient createService(String serviceName, List<?> args, ContainerStateObserver observer) {
        String[] envVariables = args.stream().map(Object::toString).collect(Collectors.toList()).toArray(new String[0]);

        String result = connector.createContainer(serviceName, envVariables, observer);
        return result;
    }
}
