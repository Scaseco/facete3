package org.hobbit.core.service.docker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

public class DockerServiceSimpleDelegation
    extends AbstractIdleService
    implements DockerService
{
	private static final Logger logger = LoggerFactory.getLogger(DockerServiceSimpleDelegation.class);
	
    protected String imageName;
    protected Map<String, String> localEnvironment;// = new LinkedHashMap<>();

    protected BiFunction<String, Map<String, String>, String> startServiceDelegate;

    // Function to stop a container. Argument is the container id
    protected Consumer<String> stopServiceDelegate;

    protected String containerId;

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
    protected void startUp() throws Exception {
    	nameThreadForAction(imageName, () -> {
    		containerId = startServiceDelegate.apply(imageName, localEnvironment);
    	});
    }

    @Override
    protected void shutDown() throws Exception {
    	nameThreadForAction(imageName, () -> {
            stopServiceDelegate.accept(containerId);
    	});
    }

    @Override
    public int getExitCode() {
        // TODO Auto-generated method stub
        return 0;
    }

}
