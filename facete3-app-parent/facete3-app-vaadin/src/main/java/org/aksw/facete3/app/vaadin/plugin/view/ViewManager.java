package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import com.vaadin.flow.component.Component;

/**
 * A manager of factories for creating views over data.
 * The manager needs background knowledge to in order to classify
 * a given Node w.r.t. to the specifications held by the view factories.
 *
 *
 * @author raven
 */
public interface ViewManager {
    void register(ViewFactory viewFactory);

    ViewFactory getBestViewFactory(Node node);
    Component getComponent(Node node);
    Resource fetchData(Node node, ViewFactory viewFactory);
    List<ViewFactory> getApplicableViewFactories(Node node);

    /*
     * Bulk operations
     */

    Map<Node, ViewFactory> getBestViewFactories(Collection<Node> nodes);
    Map<Node, Component> getComponents(Collection<Node> nodes);
    Map<Node, Resource> fetchData(Collection<Node> nodes, ViewFactory viewFactory);
    Map<Node, List<ViewFactory>> getApplicableViewFactories(Collection<Node> nodes);
}
