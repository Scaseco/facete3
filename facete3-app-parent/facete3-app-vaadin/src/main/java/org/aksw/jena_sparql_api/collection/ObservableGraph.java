package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import org.apache.jena.graph.Graph;

public interface ObservableGraph
    extends Graph
{
    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);
}
