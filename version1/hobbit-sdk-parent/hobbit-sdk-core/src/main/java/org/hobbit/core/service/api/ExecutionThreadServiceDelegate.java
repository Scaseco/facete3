package org.hobbit.core.service.api;

import org.hobbit.core.service.docker.impl.core.DockerServiceSimpleDelegation;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class ExecutionThreadServiceDelegate<T extends RunnableServiceCapable>
    extends AbstractExecutionThreadService
    implements ServiceDelegateEntity<T>
{
	protected T delegate;

    public ExecutionThreadServiceDelegate(T delegate) {
        super();
        this.delegate = delegate;
    }
    
    @Override
    public T getEntity() {
    	return delegate;
    }

    @Override
    protected void startUp() throws Exception {
    	DockerServiceSimpleDelegation.nameThreadForAction(delegate.getClass().getName(), () -> {
    		super.startUp();
    		delegate.startUp();
    		return null;
    	});
    }

    @Override
    protected void run() throws Exception {
    	DockerServiceSimpleDelegation.nameThreadForAction(delegate.getClass().getName(), () -> {
    		delegate.run();
    		return null;
    	});
    }

    @Override
    protected void triggerShutdown() {
    	DockerServiceSimpleDelegation.nameThreadForAction(delegate.getClass().getName(), () -> {
	    	try {
				delegate.shutDown();
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				super.triggerShutdown();
			}
    	});
    }

	@Override
	public String toString() {
		return "ExecutionThreadServiceDelegate [delegate=" + delegate + "]";
	}
    
//    @Override
//    protected void shutDown() throws Exception {
//		delegate.shutDown();
//    	super.shutDown();
//    }
}
