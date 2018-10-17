package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class WeigthedSelectorDrawWithReplacement<T>
	extends WeightedSelectorMutable<T>
{
	public WeigthedSelectorDrawWithReplacement() {
		super();
	}

	public WeigthedSelectorDrawWithReplacement(List<Entry<T, Double>> entries) {
		super(entries);
	}

	public WeightedSelectorMutable<T> clone() {
		return new WeigthedSelectorDrawWithReplacement<T>(new ArrayList<>(entries));
	}

	@Override
	public Entry<T, Double> sampleEntry(Double t) {
		Entry<Integer, Entry<T, Double>> e = sampleEntryWithIndex(t);

		Entry<T, Double> result = null;
		if(e != null) {
			int index = e.getKey();
			entries.remove(index);
			result = e.getValue();
		}

		return result;
	}

	public static <T> WeigthedSelectorDrawWithReplacement<T> create(Collection<Entry<T, Double>> entries) {
		return new WeigthedSelectorDrawWithReplacement<>(new ArrayList<>(entries));
	}
}
