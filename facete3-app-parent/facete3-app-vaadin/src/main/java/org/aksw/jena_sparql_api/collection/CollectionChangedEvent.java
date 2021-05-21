package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

public abstract class CollectionChangedEvent<T>
    extends PropertyChangeEvent
{
    public CollectionChangedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    abstract Collection<T> getAdditions();
    abstract Collection<T> getDeletions();
    abstract Collection<T> getRefreshes();

    public boolean hasChanges() {
        boolean result = !(getAdditions().isEmpty() && getDeletions().isEmpty() && getRefreshes().isEmpty());
        return result;
    }

    // added items
    // removed items
    // refreshed items
}
