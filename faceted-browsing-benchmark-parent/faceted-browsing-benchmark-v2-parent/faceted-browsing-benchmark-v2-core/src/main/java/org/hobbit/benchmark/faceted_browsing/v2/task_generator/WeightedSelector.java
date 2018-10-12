package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public interface WeightedSelector<T>
	extends Cloneable
{
//	@Override
	/**
	 * Cloning mechanism to allow simple implementation of backtracking.
	 * 
	 * @return
	 */
	WeightedSelector<T> clone();

	Entry<T, Double> sampleEntry(Double t);
	
	// Note: Collection is allowed to contain duplicates
	Collection<Entry<T, Double>> entries();
	

	default T sample(Double t) {
		Entry<T, Double> e = sampleEntry(t);
		T result = e != null ? e.getKey() : null;
		return result;
	}

	// Derived map that sums up the weights of each item
	default Map<T, Double> entryMap() {
		Map<T, Double> result = entries().stream()
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> a + b));
		return result;
	}
	
	default boolean isEmpty() {
		Collection<Entry<T, Double>> tmp = entries();
		boolean result = tmp.isEmpty();
		return result;
	}
}
