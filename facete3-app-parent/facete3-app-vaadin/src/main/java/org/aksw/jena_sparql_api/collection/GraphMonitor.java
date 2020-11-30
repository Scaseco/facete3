package org.aksw.jena_sparql_api.collection;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetChanges;
import org.apache.jena.sparql.core.QuadAction;
import org.apache.jena.sparql.graph.GraphWrapper;


/**
 * A change tracking wrapper for a graph.
 *
 * The add / delete methods are overridden so that appropriate events can be sent out.
 * Events can be send out immediately on every change or manually until firePendingEvents() is called.
 *
 * The delegate graph should not be changed directly.
 *
 * @author raven
 *
 */
public class GraphMonitor extends GraphWrapper
{
    /** Whether to see if a quad action will change the dataset - test before add for existence, test before delete for absence */
    private boolean CheckFirst = true ;
    /** Whether to record a no-op (maybe as a comment) */
    private boolean RecordNoAction = true ;
//    /** Where to send the notifications */
//    private final DatasetChanges monitor ;

    /**
     * Create a DatasetGraph wrapper that monitors the dataset for changes (add or delete quads).
     * Use this DatasetGraph for all operations in order to record changes.
     * Note whether additions of deletions cause an actual change to the dataset or not.
     * @param dsg       The DatasetGraph to monitor
     * @param monitor   The handler for a change
     *
     * @see DatasetChanges
     * @see QuadAction
     */
    public GraphMonitor(Graph delegate)
    {
        super(delegate) ;
    }


    public void postponeEvents(boolean onOrOff) {

    }

    public void firePostponedEvents() {

    }

    /**
     * Create a DatasetGraph wrapper that monitors the dataset for changes (add or delete quads).
     * Use this DatasetGraph for all operations in order to record changes.
     * @param dsg       The DatasetGraph to monitor
     * @param monitor   The handler for a change
     * @param recordOnlyIfRealChange
     *         If true, check to see if the change would have an effect (e.g. add is a new quad).
     *         If false, log changes as ADD/DELETE regardless of whether the dataset actually changes.
     *
     * @see DatasetChanges
     * @see QuadAction
     */
//    public DatasetGraphMonitor(DatasetGraph dsg, DatasetChanges monitor, boolean recordOnlyIfRealChange)
//    {
//        super(dsg) ;
//        CheckFirst = recordOnlyIfRealChange ;
//        this.monitor = monitor ;
//    }

    /** Return the monitor */
//    public DatasetChanges getMonitor()      { return monitor ; }

    /** Return the monitored DatasetGraph */
//    public DatasetGraph   monitored()       { return getWrapped() ; }

    @Override public void add(Triple quad)
    {
        if ( CheckFirst && contains(quad) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_ADD, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        add$(quad) ;
    }

//    @Override public void add(Node s, Node p, Node o)
//    {
//        if ( CheckFirst && contains(s,p,o) )
//        {
//            if ( RecordNoAction )
//                record(QuadAction.NO_ADD,g,s,p,o) ;
//            return ;
//        }
//
//        add$(g,s,p,o) ;
//    }

//    private void add$(Node s, Node p, Node o)
//    {
//        super.add(g,s,p,o) ;
//        record(QuadAction.ADD,s,p,o) ;
//    }

    private void add$(Triple quad)
    {
        super.add(quad) ;
        record(QuadAction.ADD, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    @Override public void delete(Triple quad)
    {
        if ( CheckFirst && ! contains(quad) )
        {
            if ( RecordNoAction )
                record(QuadAction.NO_DELETE, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
            return ;
        }
        delete$(quad) ;
    }

//    @Override public void delete(Node s, Node p, Node o)
//    {
//        if ( CheckFirst && ! contains(s,p,o) )
//        {
//            if ( RecordNoAction )
//                record(QuadAction.NO_DELETE, g,s,p,o) ;
//            return ;
//        }
//        delete$(g,s,p,o) ;
//    }

    private void delete$(Triple quad)
    {
        super.delete(quad) ;
        record(QuadAction.DELETE, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

//    private void delete$(Node s, Node p, Node o)
//    {
//        super.delete(s,p,o) ;
//        record(QuadAction.DELETE,s,p,o) ;
//    }


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

//    @Override public void addGraph(Node gn, Graph g)
//    {
//        // Convert to quads.
//        //super.addGraph(gn, g) ;
//        ExtendedIterator<Triple> iter = g.find(Node.ANY, Node.ANY, Node.ANY) ;
//        for ( ; iter.hasNext(); )
//        {
//            Triple t = iter.next() ;
//            add(gn, t.getSubject(), t.getPredicate(), t.getObject()) ;
//        }
//    }
//
//    @Override public void removeGraph(Node gn)
//    {
//        //super.removeGraph(gn) ;
//        deleteAny(gn, Node.ANY, Node.ANY, Node.ANY) ;
//    }

    private void record(QuadAction action, Node s, Node p, Node o)
    {
//    	GraphEventManager em = new SimpleEventManager();
//    	em.

//        monitor.change(action, g, s, p, o) ;
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
