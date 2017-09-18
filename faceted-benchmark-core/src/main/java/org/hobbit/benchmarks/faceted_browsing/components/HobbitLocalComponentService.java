package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.hobbit.interfaces.BaseComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.AbstractIdleService;

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

    @Autowired
    protected ObservableByteChannel commandChannel;


    protected transient T component;
    protected transient Consumer<ByteBuffer> observer;

    public HobbitLocalComponentService(Class<T> componentClass, ApplicationContext ctx,
            ObservableByteChannel commandChannel) {
        super();
        this.componentClass = componentClass;
        this.ctx = ctx;
        this.commandChannel = commandChannel;
    }

    public T getComponent() {
        return component;
    }

    @Override
    protected void startUp() throws Exception {

        logger.debug("Starting local component of type " + componentClass);
        component = componentClass.newInstance();
        ctx.getAutowireCapableBeanFactory().autowireBean(component);

        observer = buffer -> PseudoHobbitPlatformController.forwardToHobbit(buffer, component::receiveCommand);

        component.init();
        logger.debug("Successfully started local component of type " + componentClass);

    }

    @Override
    protected void shutDown() throws Exception {
        IOUtils.closeQuietly(component);

        // After the benchmark controller served its purpose, deregister it from events
        commandChannel.removeObserver(observer);
    }
}