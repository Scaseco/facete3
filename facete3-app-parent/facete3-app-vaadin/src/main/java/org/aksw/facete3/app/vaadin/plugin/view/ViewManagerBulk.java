package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import com.vaadin.flow.component.Component;

/** Interface with default methods that delegate to the respective bulk versions. */
public interface ViewManagerBulk
    extends ViewManager
{
    @Override
    default ViewFactory getBestViewFactory(Node node) {
        return getBestViewFactories(Collections.singleton(node)).get(node);
    }

    @Override
    default Component getComponent(Node node) {
        return getComponents(Collections.singleton(node)).get(node);
    }

    @Override
    default Resource fetchData(Node node, ViewFactory viewFactory) {
        return fetchData(Collections.singleton(node), viewFactory).get(node);
    }

    @Override
    default List<ViewFactory> getApplicableViewFactories(Node node) {
        return getApplicableViewFactories(Collections.singleton(node)).getOrDefault(node, Collections.emptyList());
    }
}
