package org.hobbit.core.service.docker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractService;

/**
 * Docker service wrapper that delegates start/stop request to lambdas. 
 * This delegate does not use threads.
 * 
 * @author raven Apr 26, 2018
 *
 */
public class DockerServiceSimpleDelegation
    extends AbstractService
    implements DockerService
{
	private static final Logger logger = LoggerFactory.getLogger(DockerServiceSimpleDelegation.class);
	
    protected String imageName;
    protected Map<String, String> localEnvironment;// = new LinkedHashMap<>();

    protected BiFunction<String, Map<String, String>, String> startServiceDelegate;

    // Function to stop a container. Argument is the container id
    protected Consumer<String> stopServiceDelegate;

    protected String containerId;
    protected Integer exitCode;

    public DockerServiceSimpleDelegation(String imageName,
    		Map<String, String> localEnvironment,
    		BiFunction<String, Map<String, String>, String> startServiceDelegate,
            Consumer<String> stopServiceDelegate) {
        this.imageName = imageName;
        this.localEnvironment = localEnvironment;
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

    public static void nameThreadForAction(String name, Runnable runnable) {
    	try {
			nameThreadForAction(name, () -> {
				runnable.run();
				return null;
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    public static void nameThreadForAction(String name, Callable<Void> runnable) throws Exception {
    	String threadName = Thread.currentThread().getName();
    	Thread.currentThread().setName(threadName + " [" + name + "]");

    	try {
    		logger.info("[Begin of action] " + name);
    		runnable.call();
    		logger.info("[End of action] " + name);
    	} finally {
    		Thread.currentThread().setName(threadName);
    	}
    }
    
    @Override
    protected void doStart() {
//    	nameThreadForAction(imageName, () -> {
    	try {
    		containerId = startServiceDelegate.apply(imageName, localEnvironment);
    		notifyStarted();
    	} catch(Exception e) {
    		notifyFailed(e);
    	}
    		
//    	});
    }

    @Override
    protected void doStop() {
//    	nameThreadForAction(imageName, () -> {
    	try {
    		stopServiceDelegate.accept(containerId);
    		notifyStopped();
    	} catch(Exception e) {
    		notifyFailed(e);
    	}
//    	});
    }
    
    /**
     * Method to externally declare a failure on this service delegate
     * 
     * @param cause
     */
    public void declareFailure(Throwable cause) {
    	notifyFailed(cause);
    }
    
    
    public void setExitCode(int exitCode) {
    	this.exitCode = exitCode;
    }

    @Override
    public Integer getExitCode() {
        return exitCode;
    }

}

