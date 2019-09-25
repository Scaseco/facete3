package org.hobbit.benchmark.faceted_browsing.v2.main;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

/**
 * Service wrapper that allows post start and post stop action to be appended
 * to an existing service.
 * The service wrapper is only considered started/stopped when the post
 * action is completed.
 * 
 * @author raven
 *
 * @param <S>
 */
public class AbstractServiceDelegate<S extends Service>
    extends AbstractService
{
    protected S delegate;

    public AbstractServiceDelegate(S delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    protected void doStart() {
        delegate.addListener(new Listener() {
            @Override
            public void running() {
                try {
                    afterStart();
                } catch(Exception e) {
                    delegate.stopAsync();
                    notifyFailed(e);
                }
                
                notifyStarted();
            }
            @Override
            public void failed(State priorState, Throwable t) {
                notifyFailed(t);
            }

            @Override
            public void terminated(State priorState) {
                try {
                    afterStop();
                } finally {
                    notifyStopped();
                }
            }
        }, MoreExecutors.directExecutor());
        
        delegate.startAsync();
    }

    @Override
    protected void doStop() {
        delegate.stopAsync();
    }
    
    public void afterStart() {
    }
    
    public void afterStop() {            
    }        
}

