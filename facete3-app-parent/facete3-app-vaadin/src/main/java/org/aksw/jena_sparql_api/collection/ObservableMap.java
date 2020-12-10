package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Map;

public interface ObservableMap<K, V>
    extends Map<K, V>
{
    @Override
    ObservableSet<K> keySet();

//    @Override
//    ObservableSet<Entry<K, V>> entrySet();
//
//    @Override
//    ObservableCollection<V> values();

    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Runnable addPropertyChangeListener(PropertyChangeListener listener);
}

