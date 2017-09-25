package org.hobbit.core.services;

import com.google.common.util.concurrent.AbstractIdleService;

public class IdleServiceDelegate
    extends AbstractIdleService
{
    protected Runnable startUp;
    protected Runnable shutDown;


    public IdleServiceDelegate(Runnable startUp, Runnable shutDown) {
        super();
        this.startUp = startUp;
        this.shutDown = shutDown;
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

