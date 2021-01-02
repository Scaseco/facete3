package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


/**
 * Getter/setter view over an observable collection.
 * If that collection has a single item then {@link #get()} method returns it. Otherwise, if there are
 * no or multiple items then the method returns null.
 * 
 * @author raven
 *
 * @param <T>
 */
public class ObservableValueFromObservableCollection<T, U>
    implements ObservableValue<T>
{
    protected ObservableCollection<U> delegate;
    protected Function<? super Collection<? extends U>, ? extends T> xform;
    protected Function<? super T, ? extends U> valueToItem;

    public ObservableValueFromObservableCollection(
    		ObservableCollection<U> delegate,
    		Function<? super Collection<? extends U>, ? extends T> xform,
    		Function<? super T, ? extends U> valueToItem) {
        super();
        this.delegate = delegate;
        this.xform = xform;
        this.valueToItem = valueToItem;
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
        T result = xform.apply(delegate);
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
    	T oldValue = get();
    	if (!Objects.equals(oldValue, value)) {    	
	        delegate.clear();
	        U item = valueToItem.apply(value);
	        if (item != null) {
	        	delegate.add(item);
	        }
    	}
    }

    /** Wrap the listener so that the set-based property change event is
     * converted to a single value based on */
    public static <T, U> PropertyChangeListener wrapListener(
    		Object self, PropertyChangeListener listener,
    		Function<? super Collection<? extends U>, ? extends T> xform
    		) {
        return ev -> {
            T oldValue = Optional.ofNullable((Collection<U>)ev.getOldValue())
//                    .map(ObservableValueFromObservableCollection::getOnlyElementOrNull)
                    .map(xform)
                    .orElse(null);

            T newValue = Optional.ofNullable(((Collection<U>)ev.getNewValue()))
                    .map(xform)
                    .orElse(null);

            PropertyChangeEvent newEv = new PropertyChangeEvent(
                    self, "value", oldValue, newValue);
            listener.propertyChange(newEv);
        };
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return delegate.addPropertyChangeListener(wrapListener(this, listener, xform));
    }

    public static <T> ObservableValue<T> decorate(ObservableCollection<T> delegate) {
        return new ObservableValueFromObservableCollection<T, T>(delegate, ObservableValueFromObservableCollection::getOnlyElementOrNull, x -> x);
    }

}
