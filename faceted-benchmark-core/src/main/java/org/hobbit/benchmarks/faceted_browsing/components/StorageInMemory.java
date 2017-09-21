package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Sets;

public class StorageInMemory<K, V>
    implements Storage<K, V>
{

    protected Map<K, V> keyToExpectedValue = new LinkedHashMap<>();
    protected Map<K, V> keyToActualValue = new LinkedHashMap<>();

    @Override
    public void putExpectedValue(K key, V value) {
        keyToExpectedValue.put(key, value);
    }

    @Override
    public void putActualValue(K key, V value) {
        keyToActualValue.put(key, value);
    }




    @Override
    public Stream<Entry<K, Entry<V, V>>> streamResults() {
        return streamPairs(keyToExpectedValue, keyToActualValue);
    }

    /**
     * Creates a stream of entries of the form (keyCommonToBothMaps, (valueForKeyInA, valueForkeyInB))
     *
     * @param a
     * @param b
     * @return
     */
    public static <K, V> Stream<Entry<K, Entry<V, V>>>
        streamPairs(Map<K, V> a, Map<K, V> b)
    {
        Set<K> keys = Sets.union(a.keySet(), b.keySet());

        Stream<Entry<K, Entry<V, V>>> result = keys.stream()
                .map(key -> new SimpleEntry<>(key, new SimpleEntry<>(
                        a.get(key),
                        b.get(key))));

        return result;
    }
}
