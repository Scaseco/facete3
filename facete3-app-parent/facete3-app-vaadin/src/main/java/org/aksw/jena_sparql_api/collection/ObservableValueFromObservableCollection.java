package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Optional;


/**
 * Getter/setter view over an observable collection.
 * If that collection has a single item then {@link #get()} method returns it. Otherwise, if there are
 * no or multiple items then the method returns null.
 *
 * @author raven
 *
 * @param <T>
 */
public class ObservableValueFromObservableCollection<T>
    implements ObservableValue<T>
{
    protected ObservableCollection<T> delegate;

    public ObservableValueFromObservableCollection(ObservableCollection<T> delegate) {
        super();
        this.delegate = delegate;
    }

    public static <T> T getOnlyElementOrNull(Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();
        T result = it.hasNext() ? it.next() : null;

        if (it.hasNext()) {
            result = null;
            // throw new IllegalStateException();
        }

        return result;
    }

    /**
     * Attempt to get the only element from the underlying collection. If that collection
     * is empty or contains multiple (possible equal) elements then return null.
     */
    @Override
    public T get() {
        T result = getOnlyElementOrNull(delegate);
        return result;
    }

    /* TODO Add a get method that raises an exception:
     *   If there are multiple elements (even if they are equal)
     *   an {@link IllegalStateException} is raised.
     */

    /**
     * First clear the underlying collection.
     * If the given value is non-null then add it to the collection.
     */
    @Override
    public void set(T value) {
        delegate.clear();
        if (value != null) {
            delegate.add(value);
        }
    }

    /** Wrap the listener so that the set-based property change event is
     * converted to a single value based on */
    public static PropertyChangeListener wrapListener(Object self, PropertyChangeListener listener) {
        return ev -> {
            Object oldValue = Optional.ofNullable(((Iterable<?>)ev.getOldValue()))
                    .map(ObservableValueFromObservableCollection::getOnlyElementOrNull)
                    .orElse(null);

            Object newValue = Optional.ofNullable(((Iterable<?>)ev.getNewValue()))
                    .map(ObservableValueFromObservableCollection::getOnlyElementOrNull)
                    .orElse(null);

            PropertyChangeEvent newEv = new PropertyChangeEvent(
                    self, "value", oldValue, newValue);
            listener.propertyChange(newEv);
        };
    }

    @Override
    public Runnable addListener(PropertyChangeListener listener) {
        return delegate.addListener(wrapListener(this, listener));
    }

    public static <T> ObservableValue<T> decorate(ObservableCollection<T> delegate) {
        return new ObservableValueFromObservableCollection<T>(delegate);
    }

}
