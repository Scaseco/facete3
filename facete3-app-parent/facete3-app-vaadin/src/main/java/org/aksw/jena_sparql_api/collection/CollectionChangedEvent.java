package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;

public class CollectionChangedEvent<T>
    extends PropertyChangeEvent
{
    public CollectionChangedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }
    // added items
    // removed items
    // refreshed items
}
