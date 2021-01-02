package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Map;
import java.util.Objects;

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


    default ObservableValue<V> observeKey(K key) {
        return new ObservableValue<V>() {
            // protected K k = key;

            @Override
            public V get() {
                return ObservableMap.this.get(key);
            }

            @Override
            public void set(V value) {
                ObservableMap.this.put(key, value);
            }

            @Override
            public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
                return ObservableMap.this.addPropertyChangeListener(ev -> {
                    V oldValue = ((Map<K, V>)ev.getOldValue()).get(key);
                    V newValue = ((Map<K, V>)ev.getNewValue()).get(key);

                    if (oldValue != null && newValue != null && !Objects.equals(oldValue, newValue)) {
                        listener.propertyChange(new PropertyChangeEvent(this, "value", oldValue, newValue));
                    }

                });
            }
        };
    }
}

