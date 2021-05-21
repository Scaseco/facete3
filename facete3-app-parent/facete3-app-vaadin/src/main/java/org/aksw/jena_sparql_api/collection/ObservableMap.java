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

    
    /**
     * Observe a key's value
     * 
     * @param key
     * @return
     */
    default ObservableValue<V> observeKey(K key) {
    	return observeKey(key, null);
    }

    /**
     * Observe a key's value. Yield a default value if the key does not exist or its value is null.
     * 
     * @param key
     * @return
     */
    default ObservableValue<V> observeKey(K key, V defaultValue) {
        return new ObservableValue<V>() {
            // protected K k = key;

            @Override
            public V get() {
                return ObservableMap.this.getOrDefault(key, defaultValue);
            }

            @Override
            public void set(V value) {
            	if (value == null) {
                    ObservableMap.this.remove(key);            		
            	} else {
            		ObservableMap.this.put(key, value);
            	}
            }

            @Override
            public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
                return ObservableMap.this.addPropertyChangeListener(ev -> {
                    V oldValue = ((Map<K, V>)ev.getOldValue()).getOrDefault(key, defaultValue);
                    V newValue = ((Map<K, V>)ev.getNewValue()).getOrDefault(key, defaultValue);

                    if (oldValue != null && newValue != null && !Objects.equals(oldValue, newValue)) {
                        listener.propertyChange(new PropertyChangeEvent(this, "value", oldValue, newValue));
                    }

                });
            }
        };
    }
}

