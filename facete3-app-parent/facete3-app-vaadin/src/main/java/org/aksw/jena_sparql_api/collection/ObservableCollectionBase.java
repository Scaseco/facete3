package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.ConvertingCollection;

import com.google.common.base.Converter;

public class ObservableCollectionBase<T, C extends Collection<T>>
    extends AbstractCollection<T>
    implements ObservableCollection<T>
{
    protected C backend;

    protected VetoableChangeSupport vcs = new VetoableChangeSupport(this);
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
            {
                Set<T> newItem = Collections.singleton(value);
                Collection<T> oldValue = this;
                Collection<T> newValue = CollectionOps.smartUnion(backend, newItem);

                try {
                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                            this, oldValue, newValue,
                            newItem, Collections.emptySet(), Collections.emptySet()));
                } catch (PropertyVetoException e) {
                    throw new RuntimeException(e);
                }
            }

            result = backend.add(value);

            {
                Set<T> newItem = Collections.singleton(value);
                Collection<T> oldValue = CollectionOps.smartDifference(backend, newItem);
                Collection<T> newValue = this;

                try {
                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                            this, oldValue, newValue,
                            newItem, Collections.emptySet(), Collections.emptySet()));
                } catch (PropertyVetoException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        return result;
    }


    @Override
    public boolean remove(Object o) {
        boolean result = false;
        if (isDuplicateAwareBackend() || backend.contains(o)) {
            T item;

            try {
                item = (T)o;
            } catch(ClassCastException e) {
                /* should nover happen because the item was centained */
                throw new RuntimeException(e);
            }

            {
                Set<T> removedItem = Collections.singleton(item);
                Collection<T> oldValue = backend;
                Collection<T> newValue = CollectionOps.smartDifference(backend, removedItem);

                try {
                    vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                            this, oldValue, newValue,
                            Collections.emptySet(), removedItem, Collections.emptySet()));
                } catch (PropertyVetoException e) {
                    throw new RuntimeException(e);
                }
            }

            result = backend.remove(item);

            {
                Set<T> removedItem = Collections.singleton(item);
                Collection<T> oldValue = CollectionOps.smartUnion(backend, removedItem);
                Collection<T> newValue = backend;

                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                        this, oldValue, newValue,
                        Collections.emptySet(), removedItem, Collections.emptySet()));
            }


        }

        return result;
    }

    protected static <F, B> Collection<F> convert(Collection<B> set, Converter<B, F> converter) {
        return set == null ? null : ConvertingCollection.createSafe(set, converter);
    }

    @Override
    public Iterator<T> iterator() {
        return backend.iterator();
    }

    @Override
    public int size() {
        return backend.size();
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(listener);
        return () -> vcs.removeVetoableChangeListener(listener);
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        return () -> pcs.removePropertyChangeListener(listener);
    }

}
