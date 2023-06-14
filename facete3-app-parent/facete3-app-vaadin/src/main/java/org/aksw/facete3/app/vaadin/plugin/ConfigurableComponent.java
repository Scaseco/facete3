package org.aksw.facete3.app.vaadin.plugin;

import org.aksw.vaadin.common.component.managed.ManagedComponent;

public interface ConfigurableComponent<R>
    extends ManagedComponent
{
    R getConfig();
}
