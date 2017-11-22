package org.hobbit.core.service.docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
@Deprecated
public class DockerServiceApplicationContext
    extends AbstractExecutionThreadService
    implements DockerService
{

    private static final Logger logger = LoggerFactory.getLogger(DockerServiceDockerClient.class);

    
    protected SpringApplicationBuilder appBuilder;
    protected String[] args;
    
    protected ConfigurableApplicationContext ctx;

    protected String fakeImageName;
    protected String fakeContainerId;

    public DockerServiceApplicationContext(
    		SpringApplicationBuilder appBuilder,
    		String[] args,
    		String fakeImageName,
    		String fakeContainerId) {
        super();
        this.appBuilder = appBuilder;
        this.args = args;
        this.fakeImageName = fakeImageName;
        this.fakeContainerId = fakeContainerId;
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void run() throws Exception {
    	ctx = appBuilder.run(args);
    }
    
    
    @Override
    protected void shutDown() throws Exception {
    	ctx.close();
    }

    @Override
    public String getImageName() {
    	return fakeImageName;
    }

    @Override
    public String getContainerId() {
        return fakeContainerId;
    }

    @Override
    public int getExitCode() {
        logger.warn("STUB! Exist code always returns 0");
        return 0;
    }

}

