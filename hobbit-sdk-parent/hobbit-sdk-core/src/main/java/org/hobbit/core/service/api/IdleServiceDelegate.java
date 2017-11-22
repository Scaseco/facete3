package org.hobbit.core.service.api;

import com.google.common.util.concurrent.AbstractIdleService;

public class IdleServiceDelegate<T>
    extends AbstractIdleService
    implements ServiceDelegate<T>
{
	protected T entity;
    protected Runnable startUp;
    protected Runnable shutDown;


    public IdleServiceDelegate(T entity, Runnable startUp, Runnable shutDown) {
        super();
        this.entity = entity;
        this.startUp = startUp;
        this.shutDown = shutDown;
    }

    @Override
    public T getEntity() {
    	return entity;
    }
    

    @Override
    protected void startUp() throws Exception {
        startUp.run();
    }


    @Override
    protected void shutDown() throws Exception {
        shutDown.run();
    }
}

