package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jena.ext.com.google.common.util.concurrent.MoreExecutors;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.Listener;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager;

public class ServiceManagerUtils {


    public static CompletableFuture<State> awaitState(Service service, Service.State state) {
        CompletableFuture<State> result = new CompletableFuture<>();

        Listener listener = new Listener() {

            // TODO Add other states

            @Override
            public void terminated(State from) {
                if(state.equals(State.TERMINATED)) {
                    result.complete(State.TERMINATED);
                    // TODO Remove listener - but there is no remove on service, so we would have to do things in a more complicated way...
                }
                super.terminated(from);
            }
        };

        service.addListener(listener, MoreExecutors.directExecutor());

        return result;
    }

    public static void stopAsyncAndWaitStopped(ServiceManager serviceManager, long timeout, TimeUnit unit) {
        serviceManager.stopAsync();
        try {
            serviceManager.awaitStopped(timeout, unit);
        } catch(TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startAsyncAndAwaitHealthyAndStopOnFailure(
            ServiceManager serviceManager,
            long healthyTimeout, TimeUnit healthyUnit,
            long stopTimeout, TimeUnit stopUnit)
    {

        serviceManager.startAsync();
        try {
            serviceManager.awaitHealthy(healthyTimeout, healthyUnit);
        } catch(Exception e) {
            try {
                serviceManager.stopAsync();
                serviceManager.awaitStopped(stopTimeout, stopUnit);
            } catch(Exception f) {
                throw new RuntimeException(f);
            }
            throw new RuntimeException(e);
        }

    }
}
