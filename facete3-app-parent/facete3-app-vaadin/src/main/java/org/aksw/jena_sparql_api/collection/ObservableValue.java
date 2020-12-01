package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;

import org.aksw.commons.accessors.SingleValuedAccessor;

public interface ObservableValue<T>
    extends SingleValuedAccessor<T>
{
    Runnable addListener(PropertyChangeListener listener);
}
