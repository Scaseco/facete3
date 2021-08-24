package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import org.aksw.commons.collection.observable.ObservableSet;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

public interface ObservableGraph
    extends Graph
{
    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);


    default ObservableSet<Triple> asSet() {
    	return new ObservableSetFromGraph(this);
    }
}
