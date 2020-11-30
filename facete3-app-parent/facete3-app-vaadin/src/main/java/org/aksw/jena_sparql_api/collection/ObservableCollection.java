package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.util.Collection;

public interface ObservableCollection<T>
    extends Collection<T>
{
    /** Whether to notify listeners */
    void setEnableEvents(boolean onOrOff);

    boolean isEnableEvents();

//    Runnable addListener(Consumer<CollectionChangedEvent<? super T>> listener);
    Runnable addListener(PropertyChangeListener listener);
}
