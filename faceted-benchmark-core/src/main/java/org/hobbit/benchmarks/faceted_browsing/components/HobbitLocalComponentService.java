package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.hobbit.core.services.ExecutionThreadServiceDelegate;
import org.hobbit.core.services.IdleServiceCapable;
import org.hobbit.core.services.IdleServiceDelegate;
import org.hobbit.core.services.RunnableServiceCapable;
import org.hobbit.interfaces.BaseComponent;
import org.hobbit.transfer.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;

/**
 * The service instance has a fixed environment
 *
 * @author raven
 *
 * @param <T>
 */
public class HobbitLocalComponentService<T extends BaseComponent>
    extends AbstractIdleService
{
    private static final Logger logger = LoggerFactory.getLogger(HobbitLocalComponentService.class);


    protected Class<T> componentClass;

    @Autowired
    protected ApplicationContext ctx;

    @Resource(name="commandChannel")
    protected Publisher<ByteBuffer> commandChannel;


    protected transient T componentInstance;


    // The service wrapper for the componentInstance
    protected transient Service componentService;
    protected transient Consumer<ByteBuffer> observer;

    public HobbitLocalComponentService(Class<T> componentClass, ApplicationContext ctx,
            Publisher<ByteBuffer> commandChannel) {
        super();
        this.componentClass = componentClass;
        this.ctx = ctx;
        this.commandChannel = commandChannel;
    }

    public T getComponent() {
        return componentInstance;
    }

    @Override
    protected void startUp() throws Exception {

        logger.debug("Starting local component of type " + componentClass);
        componentInstance = componentClass.newInstance();


        // Determine the appropriate service wrapper for the component
        if(componentInstance instanceof Service) {
            componentService = (Service)componentInstance;
        }
        else if(componentInstance instanceof IdleServiceCapable) {
            IdleServiceCapable tmp = (IdleServiceCapable)componentInstance;
            componentService = new IdleServiceDelegate(tmp::startUp, tmp::shutDown);
        } else if(componentInstance instanceof RunnableServiceCapable) {
            RunnableServiceCapable tmp = (RunnableServiceCapable)componentInstance;
            componentService = new ExecutionThreadServiceDelegate(tmp::startUp, tmp::run, tmp::shutDown);
        } else {
            throw new RuntimeException("Could not determine how to wrap the component as a service");
        }


        ctx.getAutowireCapableBeanFactory().autowireBean(componentInstance);

        observer = buffer -> PseudoHobbitPlatformController.forwardToHobbit(buffer, componentInstance::receiveCommand);

        commandChannel.subscribe(observer);

        //componentInstance.init();
        componentService.startAsync();
        logger.debug("Successfully started local component of type " + componentClass);

    }

    @Override
    protected void shutDown() throws Exception {
        componentService.stopAsync();
        componentService.awaitTerminated(60, TimeUnit.SECONDS);

        // After the benchmark controller served its purpose, deregister it from events
        commandChannel.unsubscribe(observer);
    }
}

