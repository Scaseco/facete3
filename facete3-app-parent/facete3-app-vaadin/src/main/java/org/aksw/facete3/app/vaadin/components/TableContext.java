package org.aksw.facete3.app.vaadin.components;

import com.google.common.cache.Cache;
import com.vaadin.flow.component.Component;

public class TableContext {
    protected Cache<Object, Component> cellToComponent; // Maybe: id-to-view-to-state?

    // We need a model where we track for each "path to a resource" the status of the view
}
