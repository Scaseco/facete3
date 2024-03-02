package org.aksw.facete3.app.vaadin.action;

import java.util.concurrent.Callable;
import java.util.function.Function;

// Interface to report progress from within a process
// Use a flowable<T> to report status updates during the lifetime of a process?
public interface ActionReport {
    void report(String message);

    void addSubTask(ProcessFactory processFactory);
}
