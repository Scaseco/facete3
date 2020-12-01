package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.Component;

public interface ComponentInstaller {
    ComponentBundle install(Component target);
}
