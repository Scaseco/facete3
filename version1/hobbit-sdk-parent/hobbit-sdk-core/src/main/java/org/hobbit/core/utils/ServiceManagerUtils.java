package org.hobbit.core.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.Listener;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager;

public class ServiceManagerUtils {

	private static final Logger logger = LoggerFactory.getLogger(ServiceManagerUtils.class);

	
	public static CompletableFuture<Object> awaitTerminatedOrStopAfterTimeout(Service service, long terminatedTimeout, TimeUnit terminatedUnit, long stopTimeout, TimeUnit stopUnit) {
		CompletableFuture<Object> result = new CompletableFuture<>();
		try {
			service.awaitTerminated(terminatedTimeout, terminatedUnit);
		} catch(Exception e) {
			service.stopAsync();
			try {
				service.awaitTerminated(stopTimeout, stopUnit);
			} catch(Exception f) {
			    f.printStackTrace();
				//throw new RuntimeException(f);
			}
			
			throw new RuntimeException(e);
		} finally {
			result.complete(true);
		}
		
		return result;
	}

    public static CompletableFuture<State> awaitState(Service service, Service.State state) {
        CompletableFuture<State> result = new CompletableFuture<>();

        Listener listener = new Listener() {

            // TODO Add other states
            @Override
            public void failed(State from, Throwable failure) {
                result.completeExceptionally(failure);
            }
            
            @Override
            public void terminated(State from) {
                if(state.equals(State.TERMINATED)) {
                    result.complete(State.TERMINATED);
                    // TODO Remove listener - but there is no remove on service, so we would have to do things in a more complicated way...
                }
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
        	logger.info("Timeout reached:\n" + serviceManager.servicesByState());
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
