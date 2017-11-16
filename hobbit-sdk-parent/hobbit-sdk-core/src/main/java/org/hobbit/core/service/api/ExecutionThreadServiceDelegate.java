package org.hobbit.core.service.api;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class ExecutionThreadServiceDelegate
    extends AbstractExecutionThreadService
{
    protected Runnable startUp;
    protected Runnable run;
    protected Runnable shutDown;

    public ExecutionThreadServiceDelegate(Runnable startUp, Runnable run, Runnable shutDown) {
        super();
        this.startUp = startUp;
        this.run = run;
        this.shutDown = shutDown;
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        if(startUp != null) {
            startUp.run();
        }
    }

    @Override
    protected void run() throws Exception {
        if(run != null) {
            run.run();
        }
    }

    @Override
    protected void shutDown() throws Exception {
        if(shutDown != null) {
            shutDown.run();
        }

        super.shutDown();
    }
}
