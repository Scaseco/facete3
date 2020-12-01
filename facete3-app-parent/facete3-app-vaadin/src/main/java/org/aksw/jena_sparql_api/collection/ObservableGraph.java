package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.QuadAction;
import org.apache.jena.sparql.graph.GraphWrapper;


/**
 * A graph wrapper that overrides the {@link #add(Triple)} and {@link #delete(Triple)} methods
 * such that duplicate insertions and removals are suppressed and thus do not fire
 * superfluous events.
 *
 * More importantly, the {@link #addEventListener(PropertyChangeListener)} method is provided
 * which fires events <b>BEFORE</b> changes occur on the graph. Hence, the old state of the graph
 * is accessible during event processing.
 * The raised events are instances of {@link CollectionChangedEventImpl} which is a subclass of
 * {@link PropertyChangeEvent}.
 *
 * Note that {@link #getEventManager()} fires events <b>AFTER</b> changes already occurred.
 *
 * @author raven
 *
 */
public class ObservableGraph extends GraphWrapper
{
    /** Whether to see if a quad action will change the dataset - test before add for existence, test before delete for absence */
    protected boolean CheckFirst = true ;
    /** Whether to record a no-op (maybe as a comment) */
    protected boolean RecordNoAction = true ;

    protected PropertyChangeSupport pce = new PropertyChangeSupport(this);


    public static ObservableGraph decorate(Graph delegate) {
        return new ObservableGraph(delegate);
    }

    public ObservableGraph(Graph delegate)
    {
        super(delegate) ;
    }

//
//    public void postponeEvents(boolean onOrOff) {
//
//    }
//
//    public void firePostponedEvents() {
//
//    }
//

    @Override public void add(Triple quad)
    {
        if ( CheckFirst && contains(quad) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_ADD, quad) ;
            return ;
        }
        add$(quad) ;
    }

    private void add$(Triple quad)
    {
        record(QuadAction.ADD, quad) ;
        super.add(quad) ;
    }

    @Override public void delete(Triple quad)
    {
        if ( CheckFirst && ! contains(quad) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_DELETE, quad) ;
            return ;
        }
        delete$(quad) ;
    }

    private void delete$(Triple quad)
    {
        record(QuadAction.DELETE, quad) ;
        super.delete(quad) ;
    }


    private static int SLICE = 1000 ;

//    @Override
//    public void deleteAny(Node s, Node p, Node o)
//    {
//        while (true)
//        {
//            Iterator<Triple> iter = find(s, p, o) ;
//            // Materialize - stops possible ConcurrentModificationExceptions
//            List<Triple> some = take(iter, SLICE) ;
//            for (Triple q : some)
//                delete$(q) ;
//            if (some.size() < SLICE) break ;
//        }
//    }

    private void record(QuadAction action, Triple t)
    {
        Set<Triple> additions;
        Set<Triple> deletions;

        switch (action) {
        case ADD:
            additions = Collections.singleton(t);
            deletions = Collections.emptySet();
            pce.firePropertyChange(new CollectionChangedEventImpl<Triple>(this, null, null, additions, deletions, null));
            break;
        case DELETE:
            additions = Collections.emptySet();
            deletions = Collections.singleton(t);
            pce.firePropertyChange(new CollectionChangedEventImpl<Triple>(this, null, null, additions, deletions, null));
            break;
        default:
            // nothing to do
            break;
        }
    }

    public Runnable addEventListener(PropertyChangeListener listener) {
        pce.addPropertyChangeListener(listener);
        return () -> pce.removePropertyChangeListener(listener);
    }

//    @Override
//    public void sync() {
//        SystemARQ.syncObject(monitor) ;
//        super.sync() ;
//    }

//    @Override
//    public Graph getDefaultGraph() {
//        return createDefaultGraph(this);
//    }
//
//    @Override
//    public Graph getGraph(Node graphNode) {
//        return createNamedGraph(this, graphNode);
//    }
}
