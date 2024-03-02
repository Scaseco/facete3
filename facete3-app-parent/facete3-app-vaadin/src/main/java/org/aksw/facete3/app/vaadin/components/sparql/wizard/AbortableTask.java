package org.aksw.facete3.app.vaadin.components.sparql.wizard;

import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;

public class AbortableTask<T> {
    // protected CompletableFuture<T>
    protected ListenableFuture<T> future;
    protected List<AbortableTask<?>> subTasks;
    protected boolean isAborted;

    public void abort() {
        if (!future.isCancelled()) {
            synchronized (this) {
                if (!future.isCancelled()) {
                    future.cancel(true);
                }
            }
        }
    }

    public void addSubTask(AbortableTask<?> subTask) {
        synchronized (this) {
            if (future.isCancelled()) {
                subTask.getFuture().cancel(true);
            }
        }
    }

    public ListenableFuture<T> getFuture() {
        return future;
    }
}
