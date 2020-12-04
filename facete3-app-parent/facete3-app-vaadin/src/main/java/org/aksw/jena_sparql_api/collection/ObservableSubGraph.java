package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.function.Predicate;

import org.aksw.facete3.app.vaadin.components.rdf.editor.TripleConstraint;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

import com.google.common.collect.Sets;



public class ObservableSubGraph
    extends GraphWithFilter
    implements ObservableGraph
{
//    public ObservableSubGraph(ObservableGraph graph, Predicate<? super Triple> predicate) {
//        super(graph, predicate);
//    }
//
//    public static ObservableSubGraph decorate(ObservableGraph graph, Predicate<? super Triple> predicate) {
//        return new ObservableSubGraph(graph, predicate);
//    }

    public ObservableSubGraph(ObservableGraph graph, TripleConstraint predicate) {
        super(graph, predicate);
    }

    public static ObservableSubGraph decorate(ObservableGraph graph, TripleConstraint predicate) {
        return new ObservableSubGraph(graph, predicate);
    }

    @Override
    public ObservableGraph get() {
        return (ObservableGraph)super.get();
    }


    public static <T> Set<T> filterSet(Set<T> set, Predicate<? super T> predicate) {
        return set == null ? null : Sets.filter(set, predicate::test);
    }

    public static CollectionChangedEventImpl<Triple> filter(Object self,
            CollectionChangedEventImpl<Triple> ev, TripleConstraint tripleConstraint) {
        return new CollectionChangedEventImpl<>(
            self,
            new GraphWithFilter((Graph)ev.getOldValue(), tripleConstraint),
            new GraphWithFilter((Graph)ev.getNewValue(), tripleConstraint),
            filterSet((Set<Triple>)ev.getAdditions(), tripleConstraint),
            filterSet((Set<Triple>)ev.getDeletions(), tripleConstraint),
            filterSet((Set<Triple>)ev.getRefreshes(), tripleConstraint));
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return get().addPropertyChangeListener(ev -> {
            CollectionChangedEventImpl<Triple> newEv = filter(this, (CollectionChangedEventImpl<Triple>)ev, predicate);
            boolean isChange = !newEv.getAdditions().isEmpty()
                    || !newEv.getDeletions().isEmpty()
                    || !newEv.getRefreshes().isEmpty();

            if (isChange) {
                listener.propertyChange(newEv);
            }
        });
    }
}
