package org.aksw.facete3.app.vaadin.plugin;

public interface ConfigurableComponent<R>
    extends ManagedComponent
{
    R getConfig();
}
