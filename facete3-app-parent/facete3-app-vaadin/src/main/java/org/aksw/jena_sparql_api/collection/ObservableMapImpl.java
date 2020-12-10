package org.aksw.jena_sparql_api.collection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.SinglePrefetchIterator;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.common.collect.ForwardingSet;
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
    protected VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ObservableMapImpl(Map<K, V> delegate) {
        super();
        this.delegate = delegate;
    }

    public static <K, V> ObservableMap<K, V> decorate(Map<K, V> delegate) {
        return new ObservableMapImpl<>(delegate);
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(listener);
        return () -> vcs.removeVetoableChangeListener(listener);
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        return () -> pcs.removePropertyChangeListener(listener);
    }

    @Override
    public V put(K key, V value) {
        Map<K, V> newItem = Collections.singletonMap(key, value);
        Map<K, V> removedItem = Collections.emptyMap();

        if (delegate.containsKey(key)) {
            removedItem = Collections.singletonMap(key, delegate.get(key));
        }

        {
            Map<K, V> oldValue = this;
            Map<K, V> newValue = MapUtils.union(oldValue, newItem);

            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                        this, oldValue, newValue,
                        newItem.entrySet(), removedItem.entrySet(), Collections.emptySet()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        V result = delegate.put(key, value);

        {
            // FIXME The following line breaks the semantics of Map.containsKey
            // when the key of the removedItem was not present before
            Map<K, V> oldValue = removedItem.isEmpty()
                    ? Maps.filterKeys(this, k -> !Objects.equals(k, key))
                    : MapUtils.union(this, removedItem);

            Map<K, V> newValue = this;

            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                    this, oldValue, newValue,
                    newItem.entrySet(), removedItem.entrySet(), Collections.emptySet()));
        }
        return result;
    }

    @Override
    public V remove(Object key) {
        V result = null;
        if (delegate.containsKey(key)) {
            V v = delegate.get(key);
            @SuppressWarnings("unchecked")
            K k = (K)key;
            doRemoveEntry(k, v);
        }

        return result;
    }


    protected V doRemoveEntry(K key, V v) {
        Map<K, V> removedItem = Collections.singletonMap(key, v);

        {
            Map<K, V> oldValue = this;
            Map<K, V> newValue = Maps.filterKeys(delegate, k -> !Objects.equals(k, key));
            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(this, oldValue, newValue, Collections.emptySet(), removedItem.entrySet(), Collections.emptySet()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        V result = delegate.remove(key);

        {
            Map<K, V> oldValue = MapUtils.union(this, removedItem);
            Map<K, V> newValue = this;
            pcs.firePropertyChange(new CollectionChangedEventImpl<>(this, oldValue, newValue, Collections.emptySet(), removedItem.entrySet(), Collections.emptySet()));
        }

        return result;
    }

    protected void notifyClear() {
        pcs.firePropertyChange(new CollectionChangedEventImpl<>(this,
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
                        doRemoveEntry(item.getKey(), item.getValue());
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

//    public static

    public class ObservableKeySet
        extends ForwardingSet<K>
        implements ObservableSet<K>
    {
        @Override
        protected Set<K> delegate() {
            return delegate.keySet();
        }

        public CollectionChangedEventImpl<K> convertEvent(PropertyChangeEvent event) {
            CollectionChangedEventImpl<Entry<K, V>> ev = (CollectionChangedEventImpl<Entry<K, V>>)event;

//            Set<Entry<K, V>> additions = (Set<Entry<K, V>>)ev.getAdditions();
//            Set<Entry<K, V>> deletions = (Set<Entry<K, V>>)ev.getDeletions();

            @SuppressWarnings("unchecked")
            Set<K> oldKeySet = ((Map<K, V>)ev.getOldValue()).keySet();
            @SuppressWarnings("unchecked")
            Set<K> newKeySet = ((Map<K, V>)ev.getNewValue()).keySet();

            CollectionChangedEventImpl<K> result = new CollectionChangedEventImpl<>(
                    this,
                    oldKeySet,
                    newKeySet,
                    Sets.difference(newKeySet, oldKeySet),
                    Sets.difference(oldKeySet, newKeySet),
                    Collections.emptySet());

            return result;
        }

        @Override
        public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
            return ObservableMapImpl.this.addVetoableChangeListener(event -> {
                CollectionChangedEventImpl<K> newEv = convertEvent(event);
                if (newEv.hasChanges()) {
                    listener.vetoableChange(newEv);
                }
            });
        }

        @Override
        public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
            return ObservableMapImpl.this.addPropertyChangeListener(event -> {
                CollectionChangedEventImpl<K> newEv = convertEvent(event);
                if (newEv.hasChanges()) {
                    listener.propertyChange(newEv);
                }
            });
        }
    }

    @Override
    public ObservableSet<K> keySet() {
        return new ObservableKeySet();
    }


    @Override
    public Collection<V> values() {
        return super.values();
    }


    public static void main(String[] args) {
        ObservableMap<String, String> map = ObservableMapImpl.decorate(new LinkedHashMap<>());
        ObservableSet<String> set = map.keySet();

        set.addPropertyChangeListener(ev -> System.out.println("KeySet changed: " + ev));

        map.addPropertyChangeListener(event -> {
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
