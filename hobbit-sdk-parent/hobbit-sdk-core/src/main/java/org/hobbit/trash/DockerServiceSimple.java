package org.hobbit.trash;

import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.hobbit.core.service.docker.DockerService;
import org.hobbit.core.service.docker.DockerServiceDockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * A DockerService backed by spotify's docker client
 *
 *
 * TODO Make scheduler for polling health status configurable, at present it checks every 10 seconds
 *
 * @author raven Sep 20, 2017
 *
 */
public class DockerServiceSimple<A, B>
    extends AbstractExecutionThreadService
    implements DockerService
{

    private static final Logger logger = LoggerFactory.getLogger(DockerServiceDockerClient.class);

    
    protected String imageName;

    
    protected Supplier<A> start;
    protected Function<A, String> getContainerId;
    protected Function<A, B> run;
    protected BiConsumer<A, B> stop;
        
    protected String containerId;
    protected A a;
    protected B b;
    

    public DockerServiceSimple(
    		Supplier<A> start,
    		Function<A, String> getContainerId,
    		Function<A, B> run,
    		BiConsumer<A, B> stop,
    		String imageName) {
        super();
        this.start = start;
        this.getContainerId = getContainerId;
        this.run = run;
        this.stop = stop;
        this.imageName = imageName;
        //this.containerId = fakeContainerId;
    }


//	public DockerServiceSimple(Supplier<Entry<String, A>> start2, Function<Entry<String, A>, String> getContainerId2,
//			Function<Entry<String, A>, B> run2, BiConsumer<Entry<String, A>, B> stop2, String imageName2) {
//		// TODO Auto-generated constructor stub
//	}


	@Override
    protected void startUp() throws Exception {
    	a = start.get();
    	containerId = getContainerId.apply(a);
    }

    @Override
    protected void run() throws Exception {
    	b = run.apply(a);
    }
    
    @Override
    protected void shutDown() throws Exception {
    	stop.accept(a, b);
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
    public int getExitCode() {
        logger.warn("STUB! Exist code always returns 0");
        return 0;
    }

}

