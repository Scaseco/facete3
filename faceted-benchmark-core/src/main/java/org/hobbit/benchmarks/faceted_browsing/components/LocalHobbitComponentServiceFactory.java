package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.hobbit.interfaces.BaseComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import com.google.common.util.concurrent.AbstractIdleService;


/**
 * The service instance has a fixed environment
 *
 * @author raven
 *
 * @param <T>
 */
class HobbitLocalComponentService<T extends BaseComponent>
    extends AbstractIdleService
{
    protected Class<T> componentClass;

    @Autowired
    protected ApplicationContext ctx;

    @Autowired
    protected ObservableByteChannel cmdQueue;


    protected transient T component;
    protected transient Consumer<ByteBuffer> observer;


    @Override
    protected void startUp() throws Exception {

        T component = componentClass.newInstance();
        ctx.getAutowireCapableBeanFactory().autowireBean(component);

        observer = buffer -> PseudoHobbitPlatformController.forwardToHobbit(buffer, component::receiveCommand);

    }

    @Override
    protected void shutDown() throws Exception {
        IOUtils.closeQuietly(component);

        // After the benchmark controller served its purpose, deregister it from events
        cmdQueue.removeObserver(observer);
    }
}


/**
 * The service factory enables modification of the environment
 *
 * @author raven
 *
 * @param <T>
 */
public class LocalHobbitComponentServiceFactory<T extends BaseComponent>
    extends AbstractSimpleServiceFactory<T>
{
    protected Class<T> componentClass;

    @Autowired
    protected ApplicationContext ctx;

    @Autowired
    protected ObservableByteChannel cmdQueue;

    @Override
    public T get() {

        try {
            //StandardEnvironment env = new StandardEnvironment();
            //env.getPropertySources().

            // Fake a new environment
            AnnotationConfigApplicationContext childCtx = new AnnotationConfigApplicationContext();
            childCtx.setParent(ctx);
            //childCtx.setEnvironment(null);

            T component = componentClass.newInstance();
            childCtx.getAutowireCapableBeanFactory().autowireBean(component);

            Consumer<ByteBuffer> observer = buffer -> PseudoHobbitPlatformController.forwardToHobbit(buffer, component::receiveCommand);

            try {
                // Register the benchmark controller as a listener to the command queue
                cmdQueue.addObserver(observer);

                //benchmarkController.executeBenchmark();
            } finally {
                IOUtils.closeQuietly(benchmarkController);

                // After the benchmark controller served its purpose, deregister it from events
                cmdQueue.removeObserver(observer);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
     }

    }

}
