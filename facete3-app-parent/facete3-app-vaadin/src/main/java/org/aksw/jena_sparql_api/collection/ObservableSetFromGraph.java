package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.aksw.facete3.app.vaadin.components.rdf.editor.TripleConstraintImpl;
import org.aksw.jena_sparql_api.utils.SetFromGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


/**
 * Set view over the values of a property of a given subject resource.
 *
 * Issue: Jena's event mechanism does not seem to allow getting actual graph changes; i.e. ignoring
 * events for redundant additions or deletions.
 * Also, there does not seem to be an integration with transaction - i.e. aborting a transaction
 * should raise an event that undos all previously raised additions/deletions.
 *
 * @author raven Nov 25, 2020
 *
 * @param <T>
 */
public class ObservableSetFromGraph
    extends SetFromGraph
    implements ObservableSet<Triple>
//    implements RdfBackedCollection<Node>
{
//    protected ObservableGraph graph;

    public ObservableSetFromGraph(ObservableGraph graph) {
        super(graph);
    }

    @Override
    public ObservableGraph getGraph() {
        return (ObservableGraph)super.getGraph();
    }

    @Override
    public boolean add(Triple t) {
//        Triple t = createTriple(node);

        boolean result = !graph.contains(t);

        if (result) {
            graph.add(t);
        }
        return result;
     }

    protected PropertyChangeEvent convertEvent(PropertyChangeEvent ev) {
        CollectionChangedEventImpl<Triple> oldEvent = (CollectionChangedEventImpl<Triple>)ev;

        return new CollectionChangedEventImpl<Triple>(
            this,
            this,
            new SetFromGraph((Graph)oldEvent.getNewValue()),
            oldEvent.getAdditions(),
            oldEvent.getDeletions(),
            oldEvent.getRefreshes()
        );
    }

    /**
     *
     * @return A Runnable that de-registers the listener upon calling .run()
     */
    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return getGraph().addPropertyChangeListener(ev -> {
            PropertyChangeEvent newEvent = convertEvent(ev);
            listener.propertyChange(newEvent);
        });
    }

}