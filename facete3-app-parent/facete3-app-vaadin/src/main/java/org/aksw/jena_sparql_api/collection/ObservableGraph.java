package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;

import org.apache.jena.graph.Graph;

public interface ObservableGraph
    extends Graph
{
    Runnable addPropertyChangeListener(PropertyChangeListener listener);
}
