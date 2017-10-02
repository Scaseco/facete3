package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.Map.Entry;
import java.util.stream.Stream;

public interface Storage<K, V> {

    void putExpectedValue(K key, V value);
    void putActualValue(K key, V value);

    // Item Structure: (key, (expectedValue, actualValue))
    Stream<Entry<K, Entry<V, V>>> streamResults();
}
