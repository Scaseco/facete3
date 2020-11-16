package org.aksw.facete3.app.vaadin.plugin;

import com.vaadin.flow.component.Component;

public interface ManagedComponent
    extends AutoCloseable
{
    Component getComponent();
    void refresh();

    @Override
    void close();
}
