package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ServiceManager;

public class ServiceManagerUtils {


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
