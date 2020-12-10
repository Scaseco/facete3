package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ObservableCollection<T>
    extends Collection<T>
{
    /** Whether to notify listeners */
//    void setEnableEvents(boolean onOrOff);

//    boolean isEnableEvents();

//    Runnable addListener(Consumer<CollectionChangedEvent<? super T>> listener);

    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);


    default ObservableCollection<T> filter(Predicate<T> predicate) {
        return null;
    }

    default <U> ObservableCollection<T> map(Function<? super T, ? extends U> predicate) {
        return null;
    }

//    default ObservableCollection<T> mapToSet(Predicate<T> predicate) {
//    	return null;
//    }

}
