package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.update.GraphListenerBatchBase;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Iterators;


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
    extends AbstractSet<Node>
    implements ObservableSet<Node>
//    implements RdfBackedCollection<Node>
{
    protected ObservableGraph graph;
    protected Node source;

//    protected PropertySchema propertySchema;
    protected Node predicate;
    protected boolean isForward;

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    // protected Set<Consumer<CollectionChangedEvent<? super Node>>> listeners = Collections.synchronizedSet(new LinkedHashSet<>());

    // protected GraphListener listener;
    protected PropertyChangeListener listener;
    protected Runnable unregisterListener = null;

//    public SetFromGraph(
//        Graph graph,
//        Node source,
//        Node predicate,
//        boolean isForward
////    	UnaryXExpr objectFilter,
////    	Supplier<>
//            ) {
//        this(subject, predicate, isForward);
//    }

    public ObservableSetFromGraph(
            ObservableGraph graph, Node source, Node predicate, boolean isForward) {
        super();

        this.graph = graph;
        this.source = source;
        this.predicate = predicate;
        this.isForward = isForward;


        Triple match = TripleUtils.createMatch(source, predicate, isForward);
        Function<Triple, Node> targetFn = t -> TripleUtils.getTarget(t, isForward);


        Set<Node> self = this;

        GraphListener oldListener = new GraphListenerBatchBase() {

            @Override
            protected void deleteEvent(Graph g, Iterator<Triple> it) {
                Set<Node> oldValue = new LinkedHashSet<>(self);

                Set<Node> changes = Streams.stream(it)
                    .filter(match::matches)
//                    .filter(g::contains)
                    .map(targetFn)
                    .collect(Collectors.toSet());

                Set<Node> newValue = Sets.difference(oldValue, changes);
                System.out.println("Delete: " + oldValue + " -> " + newValue);

                broadcastEvent(new CollectionChangedEventImpl<>(this, oldValue, newValue, null, changes, null));
            }

            @Override
            protected void addEvent(Graph g, Iterator<Triple> it) {
                Set<Node> oldValue = new LinkedHashSet<>(self);

                Set<Node> changes = Streams.stream(it)
                        .filter(match::matches)
//                        .filter(t -> !g.contains(t))
                        .map(targetFn)
                        .collect(Collectors.toSet());

                Set<Node> newValue = Sets.union(oldValue, changes);
                System.out.println("Add: " + oldValue + " -> " + newValue);

                broadcastEvent(new CollectionChangedEventImpl<>(this, oldValue, newValue, changes, null, null));
            }
        };

        listener = rawEvent -> {
            CollectionChangedEventImpl<Triple> ev = (CollectionChangedEventImpl<Triple>)rawEvent;

            if (!ev.getAdditions().isEmpty()) {
                oldListener.notifyAddIterator(graph, ev.getAdditions().iterator());
            }

            if (!ev.getDeletions().isEmpty()) {
                oldListener.notifyDeleteIterator(graph, ev.getDeletions().iterator());
            }
        };

        setEnableEvents(true);
    }

    @Override
    public synchronized void setEnableEvents(boolean onOrOff) {
        if (onOrOff) {
            if (!isEnableEvents()) {
                enableEvents();
            }
        } else {
            disableEvents();
        }
    }

    @Override
    public boolean isEnableEvents() {
        return unregisterListener != null;
    }

    protected void enableEvents() {
        if (unregisterListener == null) {
            Runnable actualUnregister = graph.addEventListener(listener);
//            GraphEventManager eventManager = graph.getEventManager();
//            eventManager.register(listener);
            unregisterListener = () -> {
                actualUnregister.run();
//                eventManager.unregister(listener);
                unregisterListener = null;
            };
        }
    }

    protected void disableEvents() {
        if (unregisterListener != null) {
            unregisterListener.run();
        }
    }


    protected void broadcastEvent(CollectionChangedEvent<Node> event) {
        pcs.firePropertyChange(event);
    }

    protected Triple createTriple(Node target) {
        Triple result = TripleUtils.create(source, predicate, target, isForward);
        return result;
    }

    @Override
    public boolean add(Node node) {
        Triple t = createTriple(node);

        boolean result = !graph.contains(t);

        if (result) {
            graph.add(t);
        }
        return result;
     }

    @Override
    public boolean contains(Object o) {
        boolean result = false;
        if(o instanceof Node) {
            Node n = (Node)o;
            Triple t = createTriple(n);
            result = graph.contains(t);
        }

        return result;
    }

    @Override
    public ExtendedIterator<Node> iterator() {
        Triple m = TripleUtils.createMatch(source, predicate, isForward);

        ExtendedIterator<Node> result = graph.find(m)
            .mapWith(t -> TripleUtils.getTarget(t, isForward));

        return result;
    }


    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }

    /**
     *
     * @return A Runnable that de-registers the listener upon calling .run()
     */
    @Override
    public Runnable addListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);

        return () -> pcs.removePropertyChangeListener(listener);
    }


    public static void main(String[] args) {
        ObservableGraph graph = new ObservableGraph(GraphFactory.createDefaultGraph());

        ObservableSet<Node> set = new ObservableSetFromGraph(graph, RDF.Nodes.first, RDFS.Nodes.label, true);

        ObservableValue<Node> value = ObservableValueFromObservableCollection.decorate(set);
        value.addListener(ev -> {
            System.out.println("Value changed: " + ev);
        });

        set.addListener(event -> {
            System.out.println(event);
        });


        graph.add(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("Hello")));
        graph.add(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("World")));
        graph.delete(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("World")));
        graph.delete(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("World")));

        graph.add(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("Cheers")));

//        Model m;
//        m.register(new ModelChangedListenero)

//        graph.getEventManager().register(listener)
        System.out.println("Items: " + set);
    }

}