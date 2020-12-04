package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.SinglePrefetchIterator;

import com.google.common.collect.Maps;

/**
 * A map the fires events on changes. Changes are also triggered on operations
 * on the {@link #entrySet()} and .entrySet().iterater().remove().
 *
 * Changes are tracked in a {@link CollectionChangedEventImpl} instance which implements {@link PropertyChangeEvent}.
 * The old value is the this map before the change, and the new value is a <b>view</b> with the pending changes applied.
 * Hence, no needless copying in performed. Event handlers need to to make copies if they want a static
 * snapshot using e.g. new LinkedHashMap<>((Map<Foo,Bar>)event.getNewValue())
 *
 * TODO keySet() entrySt and values() should return ObservableCollections.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class ObservableMapImpl<K, V>
    extends AbstractMap<K, V>
    implements ObservableMap<K, V>
{
    protected Map<K, V> delegate;
    protected PropertyChangeSupport pce = new PropertyChangeSupport(this);

    public ObservableMapImpl(Map<K, V> delegate) {
        super();
        this.delegate = delegate;
    }

    public static <K, V> ObservableMap<K, V> decorate(Map<K, V> delegate) {
        return new ObservableMapImpl<>(delegate);
    }

    @Override
    public Runnable addListener(PropertyChangeListener listener) {
        pce.addPropertyChangeListener(listener);
        return () -> pce.removePropertyChangeListener(listener);
    }

    @Override
    public V put(K key, V value) {
        Map<K, V> newItem = Collections.singletonMap(key, value);
        Map<K, V> removedItem = Collections.emptyMap();
        Map<K, V> oldValue = this;
        Map<K, V> newValue = MapUtils.union(oldValue, newItem);

        if (delegate.containsKey(key)) {
            removedItem = Collections.singletonMap(key, delegate.get(key));
        }

        pce.firePropertyChange(new CollectionChangedEventImpl<>(
                this, oldValue, newValue,
                newItem.entrySet(), removedItem.entrySet(), Collections.emptySet()));

        return delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V result = null;
        if (delegate.containsKey(key)) {
            V v = delegate.get(key);
            @SuppressWarnings("unchecked")
            K k = (K)key;
            notifyRemoveItem(k, v);

            result = delegate.remove(key);
        }

        return result;
    }


    protected void notifyRemoveItem(K key, V v) {
        Map<K, V> removedItem = Collections.singletonMap(key, v);

        Map<K, V> oldValue = this;
        Map<K, V> newValue = Maps.filterKeys(delegate, k -> !Objects.equals(k, key));

        pce.firePropertyChange(new CollectionChangedEventImpl<>(this, oldValue, newValue, Collections.emptySet(), removedItem.entrySet(), Collections.emptySet()));
    }

    protected void notifyClear() {
        pce.firePropertyChange(new CollectionChangedEventImpl<>(this,
                this, Collections.emptyMap(),
                Collections.emptySet(), this.entrySet(), Collections.emptySet()));
    }


    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public void clear() {
        notifyClear();
        delegate.clear();
    }

    /**
     * TODO The result should be an ObservableSet
     *
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {

            @Override
            public void clear() {
                notifyClear();
                delegate.clear();
            }

            @Override
            public boolean add(Entry<K, V> e) {
                boolean result = !contains(e);
                if (!result) {
                    put(e.getKey(), e.getValue());
                }

                return result;
            }

            @Override
            public boolean remove(Object o) {
                boolean result = false;
                if (o instanceof Entry) {
                    Entry<?, ?> e = (Entry<?, ?>)o;
                    result = ObservableMapImpl.this.remove(e.getKey(), e.getValue());
                }
                return result;
            }

            @Override
            public boolean contains(Object o) {
                boolean result = false;
                if (o instanceof Entry) {
                    Entry<?, ?> e = (Entry<?, ?>)o;
                    Object k = e.getKey();

                    result = ObservableMapImpl.this.containsKey(k) &&
                            Objects.equals(e.getValue(), ObservableMapImpl.this.get(k));

                }
                return result;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<K, V>> baseIt = delegate.entrySet().iterator();

                return new SinglePrefetchIterator<Entry<K, V>>() {
                    @Override
                    protected Entry<K, V> prefetch() throws Exception {
                        return baseIt.hasNext() ? baseIt.next() : finish();
                    }

                    protected void doRemove(Entry<K, V> item) {
                        notifyRemoveItem(item.getKey(), item.getValue());
                        baseIt.remove();
                    }
                };
            }

            @Override
            public int size() {
                return delegate.size();
            }

        };
    }


    public static void main(String[] args) {
        ObservableMap<String, String> map = ObservableMapImpl.decorate(new LinkedHashMap<>());
        map.addListener(event -> {
            CollectionChangedEventImpl<Entry<String, String>> ev = (CollectionChangedEventImpl<Entry<String, String>>)event;
            System.out.println("Change:");
            System.out.println("  Old Value:" + ev.getOldValue());
            System.out.println("  New Value:" + ev.getNewValue());
            System.out.println("  Added: " + ev.getAdditions() + " Removed: " + ev.getDeletions());
        });


        map.put("a", "hello");
        map.put("b", "world");
        map.put("a", "hey");
        map.clear();
    }
}
