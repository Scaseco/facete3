package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import com.vaadin.flow.component.Component;

public interface ViewManager {
    void register(ViewFactory viewFactory);

    ViewFactory getBestViewFactory(Node node);

    Component getComponent(Node node);

    Resource fetchData(Node node, ViewFactory viewFactory);

    List<ViewFactory> getApplicableViewFactories(Node node);
}
