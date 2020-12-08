package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.CollectionFromIterable;
import org.aksw.commons.collections.ConvertingCollection;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.common.base.Converter;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class ObservableCollectionBase<T, C extends Collection<T>>
    extends AbstractCollection<T>
    implements ObservableCollection<T>
{
    protected C backend;

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ObservableCollectionBase(C backend) {
        this.backend = backend;
    }

    public Collection<T> getBackend() {
        return backend;
    }

    protected boolean isDuplicateAwareBackend() {
        return !(backend instanceof Set);
    }

    @Override
    public boolean add(T value) {
        boolean result = false;

        if (isDuplicateAwareBackend() || !backend.contains(value)) {
            Set<T> newItem = Collections.singleton(value);
            Collection<T> oldValue = this;
            Collection<T> newValue = isDuplicateAwareBackend()
                    ? CollectionFromIterable.wrap(Iterables.concat(backend, newItem))
                    : Sets.union((Set<T>)backend, newItem);

            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                    this, oldValue, newValue,
                    newItem, Collections.emptySet(), Collections.emptySet()));

            result = backend.add(value);
        }

        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = false;
        try {
            if (isDuplicateAwareBackend() || backend.contains(o)) {
                T item = (T)o;
                Set<T> removedItem = Collections.singleton(item);
                Collection<T> oldValue = this;
                Collection<T> newValue = isDuplicateAwareBackend()
                        ? CollectionFromIterable.wrap(() -> Iterators.filter(
                                oldValue.iterator(),
                                PredicateFromMultisetOfDiscardedItems.create(HashMultiset.create(removedItem))::test))
                        : Sets.difference((Set<T>)backend, removedItem);

                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                        this, oldValue, newValue,
                        Collections.emptySet(), removedItem, Collections.emptySet()));

                result = backend.remove(item);
            }

        } catch(ClassCastException e) {
            /* nothing to do */
        }

        return result;
    }

    protected static <F, B> Collection<F> convert(Collection<B> set, Converter<B, F> converter) {
        return set == null ? null : ConvertingCollection.createSafe(set, converter);
    }
    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        return () -> pcs.removePropertyChangeListener(listener);
    }

    @Override
    public Iterator<T> iterator() {
        return backend.iterator();
    }

    @Override
    public int size() {
        return backend.size();
    }
}
