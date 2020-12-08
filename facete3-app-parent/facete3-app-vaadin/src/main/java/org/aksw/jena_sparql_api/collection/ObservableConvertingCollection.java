package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.aksw.commons.collections.CollectionFromIterable;
import org.aksw.commons.collections.ConvertingCollection;

import com.google.common.base.Converter;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;


public class ObservableConvertingCollection<F, B, C extends ObservableCollection<B>>
    extends ConvertingCollection<F, B, C>
    implements ObservableCollection<F>
{
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ObservableConvertingCollection(C backend, Converter<B, F> converter) {
        super(backend, converter);
    }

    protected boolean isDuplicateAwareBackend() {
        return !(backend instanceof Set);
    }

    @Override
    public boolean add(F value) {
        boolean result = false;

        B item = converter.reverse().convert(value);

        if (isDuplicateAwareBackend() || !backend.contains(item)) {
            Collection<F> newItem = Collections.singleton(value);
            Collection<F> oldValue = this;
            Collection<F> newValue = CollectionFromIterable.wrap(Iterables.concat(this, newItem));

            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                    this, oldValue, newValue,
                    newItem, Collections.emptySet(), Collections.emptySet()));

            result = backend.add(item);
        }

        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = false;
        try {
            @SuppressWarnings("unchecked")
            F frontendItem = (F)o;
            B backendItem = converter.reverse().convert(frontendItem);

            if (isDuplicateAwareBackend() || backend.contains(backendItem)) {
                Collection<F> removedItem = Collections.singleton(frontendItem);
                Collection<F> oldValue = this;
                Collection<F> newValue = CollectionFromIterable.wrap(() -> Iterators.filter(
                        oldValue.iterator(),
                        PredicateFromMultisetOfDiscardedItems.create(HashMultiset.create(removedItem))::test));

                pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                        this, oldValue, newValue,
                        Collections.emptySet(), removedItem, Collections.emptySet()));

                result = backend.remove(backendItem);
            }

        } catch(ClassCastException e) {
            /* nothing to do */
        }

        return result;
    }

    protected static <F, B> Collection<F> convert(Collection<B> set, Converter<B, F> converter) {
        return set == null ? null : ConvertingCollection.createSafe(set, converter);
    }

    @SuppressWarnings("unchecked")
    public static <F, B, C extends Collection<B>> CollectionChangedEventImpl<F> convertEvent(Object self,
            CollectionChangedEventImpl<B> ev, Converter<B, F> converter) {
        return new CollectionChangedEventImpl<F>(
            self,
            convert((Collection<B>)ev.getOldValue(), converter),
            convert((Collection<B>)ev.getNewValue(), converter),
            convert((Collection<B>)ev.getAdditions(), converter),
            convert((Collection<B>)ev.getDeletions(), converter),
            convert((Collection<B>)ev.getRefreshes(), converter));
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return getBackend().addPropertyChangeListener(ev -> {
            CollectionChangedEventImpl<F> newEv = convertEvent(this, (CollectionChangedEventImpl<B>)ev, converter);
            boolean isChange = !newEv.getAdditions().isEmpty()
                    || !newEv.getDeletions().isEmpty()
                    || !newEv.getRefreshes().isEmpty();

            if (isChange) {
                listener.propertyChange(newEv);
            }
        });
    }

}
