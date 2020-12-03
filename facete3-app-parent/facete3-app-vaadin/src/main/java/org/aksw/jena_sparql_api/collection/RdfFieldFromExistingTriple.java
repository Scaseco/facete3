package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A field that when setting its value removes the referred to triple
 * and replaces it with another one
 *
 * @author raven
 *
 * @param <T>
 */
public class RdfFieldFromExistingTriple
    implements ObservableValue<Node>
{
    protected GraphChange graph;
    protected Triple existingTriple;
    protected int componentIdx; // s p or o
    // protected PropertySchema propertySchema;

    protected Node cachedValue;

    protected PropertyChangeSupport pce = new PropertyChangeSupport(this);

    public RdfFieldFromExistingTriple(GraphChange graph, Triple existingTriple, int componentIdx) {
        super();
        this.graph = graph;
        this.existingTriple = existingTriple;
        this.componentIdx = componentIdx;

        cachedValue = getLatestValue();

        graph.addPostUpdateListener(ev -> {
            // TODO Trap potential endless loop by introducing a flag when we change the value
            // in response to an event
            set(getLatestValue());
        });
    }


    public Node getLatestValue() {
        Triple baseTriple = graph.getTripleReplacements().getOrDefault(existingTriple, existingTriple);
        List<Node> nodes = TripleUtils.tripleToList(baseTriple);
        Node result = nodes.get(componentIdx);
        return result;
    }


    @Override
    public Node get() {
        return cachedValue;
    }


    @Override
    public void set(Node value) {
        // If the original triple was remapped than take that one as the base
        Triple baseTriple = graph.getTripleReplacements().getOrDefault(existingTriple, existingTriple);
        List<Node> nodes = TripleUtils.tripleToList(baseTriple);
        nodes.set(componentIdx, value);
        Triple newTriple = TripleUtils.listToTriple(nodes);

        graph.getTripleReplacements().put(existingTriple, newTriple);

        pce.firePropertyChange("value", cachedValue, value);
        this.cachedValue = getLatestValue();
    }

    @Override
    public Runnable addListener(PropertyChangeListener listener) {
        pce.addPropertyChangeListener(listener);
        return () -> pce.removePropertyChangeListener(listener);
    }

}