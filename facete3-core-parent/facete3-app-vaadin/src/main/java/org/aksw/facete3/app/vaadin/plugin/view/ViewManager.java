package org.aksw.facete3.app.vaadin.plugin.view;

import org.apache.jena.graph.Node;

import com.vaadin.flow.component.Component;

public interface ViewManager {
    void register(ViewFactory viewFactory);

    ViewFactory getBestViewFactory(Node node);

    Component getComponent(Node node);

}
