package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;

import javax.annotation.Resource;

import org.hobbit.interfaces.BaseComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.Service;

import io.reactivex.Flowable;


/**
 * The service factory enables modification of the environment
 *
 * // The factory also creates 2 byte channel beans to the cmdQueue and the dataQueue,
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class LocalHobbitComponentServiceFactory<T extends BaseComponent>
    extends AbstractSimpleServiceFactory<Service>
{
    protected Class<T> componentClass;

    @Autowired
    protected ApplicationContext ctx;

    @Resource(name="commandChannel")
    protected Flowable<ByteBuffer> commandChannel;

    public LocalHobbitComponentServiceFactory(Class<T> componentClass) {
        super();
        this.componentClass = componentClass;
    }

    @Override
    public Service get() {

        // Note: Modifications to the local environment, such as incrementing
        // an evironment value after each invocation should be done by a wrapper
        Service result = new HobbitLocalComponentService<T>(componentClass, ctx, commandChannel);

        return result;
    }
}
