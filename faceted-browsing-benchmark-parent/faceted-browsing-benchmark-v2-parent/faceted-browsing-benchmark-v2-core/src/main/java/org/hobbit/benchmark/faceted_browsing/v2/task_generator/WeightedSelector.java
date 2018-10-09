package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Map.Entry;

public interface WeightedSelector<T> {
	Entry<T, Double> sampleEntry(Double t);
	
	default T sample(Double t) {
		Entry<T, Double> e = sampleEntry(t);
		T result = e != null ? e.getKey() : null;
		return result;
	}
}
