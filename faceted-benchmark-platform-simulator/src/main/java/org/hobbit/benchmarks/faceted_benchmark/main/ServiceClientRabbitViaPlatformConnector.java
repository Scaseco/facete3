package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.function.BiConsumer;

import org.hobbit.core.components.PlatformConnector;

public class ServiceClientRabbitViaPlatformConnector
    implements ServiceClient
{
    protected String serviceName;
    protected PlatformConnector connector;

    @Override
    public void close() throws Exception {
        connector.stopContainer(serviceName);
    }

    @Override
    public Object invoke(String name, Object... args) {
    	connector.
    	
        connector.getFactoryForOutgoingDataQueues().createDefaultRabbitQueue(name);
    }

    @Override
    public void onClose(BiConsumer<String, Integer> serviceNameAndExitCode) {
        // TODO Auto-generated method stub

    }

}
