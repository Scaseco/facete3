package org.aksw.facete3.app.vaadin.action;

import java.util.concurrent.Callable;

public interface ProcessFactory {
    Callable<?> create(ActionReport reporter);
}
