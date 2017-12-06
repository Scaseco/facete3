package org.hobbit.core.service.api;

import com.google.common.util.concurrent.AbstractIdleService;

public class IdleServiceDelegate<T extends IdleServiceCapable>
    extends AbstractIdleService
    implements ServiceDelegateEntity<T>
{
	protected T delegate;

    public IdleServiceDelegate(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getEntity() {
    	return delegate;
    }
    

    @Override
    protected void startUp() throws Exception {
        delegate.startUp();
    }


    @Override
    protected void shutDown() throws Exception {
        delegate.shutDown();
    }
}

