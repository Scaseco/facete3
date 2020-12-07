package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.aksw.jena_sparql_api.rx.GraphFactoryEx;
import org.aksw.jena_sparql_api.utils.SetFromGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.core.QuadAction;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphWrapper;


/**
 * A graph wrapper that overrides the {@link #add(Triple)} and {@link #delete(Triple)} methods
 * such that duplicate insertions and removals are suppressed and thus do not fire
 * superfluous events.
 *
 * More importantly, the {@link #addPropertyChangeListener(PropertyChangeListener)} method is provided
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
public class ObservableGraphImpl
    extends GraphWrapper
    implements ObservableGraph
{
    /** Whether to see if a quad action will change the dataset - test before add for existence, test before delete for absence */
    protected boolean CheckFirst = true ;
    /** Whether to record a no-op (maybe as a comment) */
    protected boolean RecordNoAction = true ;

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);


    public static ObservableGraphImpl decorate(Graph delegate) {
        return new ObservableGraphImpl(delegate);
    }

    public ObservableGraphImpl(Graph delegate)
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

    @Override
    public void remove(Node s, Node p, Node o) {
        deleteAny(this, Triple.createMatch(s, p, o), pcs);
    }

//    @Override
//    public void clear() {
//        deleteAny(this, Triple.createMatch(null, null, null), pcs);
//    }

    private static int SLICE = 1000 ;

    // @Override
    public static void deleteAny(
            Graph graph,
            Triple pattern,
            PropertyChangeSupport pcs
            )
    {
        int n;
        do {
            Iterator<Triple> iter = graph.find(pattern) ;

            Graph deletions = GraphFactoryEx.createInsertOrderPreservingGraph();

            for (n = 0; n < SLICE & iter.hasNext(); ++n) {
                Triple t = iter.next();
                deletions.add(t);
            }

            pcs.firePropertyChange(new CollectionChangedEventImpl<Triple>(graph,
                    graph, new Difference(graph, deletions),
                    Collections.emptySet(), new SetFromGraph(deletions), null));
        } while (n >= SLICE);
    }

    private void record(QuadAction action, Triple t)
    {
        Set<Triple> additions;
        Set<Triple> deletions;

        Graph tmp;
        switch (action) {
        case ADD:
            additions = Collections.singleton(t);
            deletions = Collections.emptySet();

            tmp = GraphFactory.createDefaultGraph();
            tmp.add(t);

            pcs.firePropertyChange(new CollectionChangedEventImpl<Triple>(this,
                    this, new Union(this, tmp),
                    additions, deletions, null));
            break;
        case DELETE:
            additions = Collections.emptySet();
            deletions = Collections.singleton(t);

            tmp = GraphFactory.createDefaultGraph();
            tmp.add(t);

            pcs.firePropertyChange(new CollectionChangedEventImpl<Triple>(this,
                    this, new Difference(this, tmp), additions, deletions, null));
            break;
        default:
            // nothing to do
            break;
        }
    }

    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        return () -> pcs.removePropertyChangeListener(listener);
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
