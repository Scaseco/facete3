package org.hobbit.benchmarks.faceted_browsing.components;

import javax.annotation.Resource;

import org.hobbit.interfaces.BaseComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.util.concurrent.Service;


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
    protected ObservableByteChannel commandChannel;

    public LocalHobbitComponentServiceFactory(Class<T> componentClass) {
        super();
        this.componentClass = componentClass;
    }

    @Override
    public Service get() {

        // TODO Add some callback to modify the environment after each invocation
        // e.g. to increment the ID of the next task generator
        Service result = new HobbitLocalComponentService<T>(componentClass, ctx, commandChannel);

        return result;
    }
}
