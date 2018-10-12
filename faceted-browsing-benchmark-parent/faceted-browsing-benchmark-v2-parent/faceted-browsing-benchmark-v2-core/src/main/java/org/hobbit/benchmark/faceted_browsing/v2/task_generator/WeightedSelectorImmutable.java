package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.common.base.Functions;

import jersey.repackaged.com.google.common.collect.Maps;

/**
 * Immutable selector over a collection of (item, weight[double]) pairs.
 * Access complexity is O(Log(n))
 * 
 * 
 * Similar to EnumeratedDistribution from commons math
 * 
 * @author Claus Stadler, Oct 12, 2018
 *
 * @param <T>
 */
public class WeightedSelectorImmutable<T>
	implements WeightedSelector<T>
{
	protected NavigableMap<Double, Entry<T, Double>> offsetToEntry;
	protected double nextOffset;

	public WeightedSelectorImmutable<T> clone() {
		return this;
	}
	
	public WeightedSelectorImmutable(NavigableMap<Double, Entry<T, Double>> offsetToEntry, double nextOffset) {
		super();
		this.nextOffset = 0.0;
	}

	@Override
	public Entry<T, Double> sampleEntry(Double t) {
		double d = Objects.requireNonNull(t).doubleValue();
		if(d < 0.0 || d > 1.0) {
			throw new IllegalArgumentException("Argument must be in the interval [0, 1]");
		}
		
		double key = t * nextOffset;
		
		Entry<T, Double> result = offsetToEntry == null || offsetToEntry.isEmpty() ? null : offsetToEntry.floorEntry(key).getValue();
		
		return result;
	}
	
	public static <X, T> WeightedSelectorImmutable<T> create(Iterable<? extends Entry<T, ? extends Number>> entries) {
		return create(entries, Entry::getKey, Entry::getValue);
	}

	public static <T> WeightedSelectorImmutable<T> create(Map<T, ? extends Number> map) {
		return create(map.entrySet());
	}

	public static <T> WeightedSelectorImmutable<T> create(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		return create(items, Functions.identity(), getWeight);
	}

	public static <X, T> WeightedSelectorImmutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		NavigableMap<Double, Entry<T, Double>> offsetToEntry = new TreeMap<>();
		
		double nextOffset = 0.0;		
		for(X item : items) {
			T entity = getEntity.apply(item);
			double itemWeight = getWeight.apply(item).doubleValue();

			Entry<T, Double> e = Maps.immutableEntry(entity, itemWeight);
			if(itemWeight < 0) {
				throw new RuntimeException("Item weights must be >= 0, encountered: " + e);
			}

			offsetToEntry.put(nextOffset, e);
			nextOffset += WeightedSelectorMutable.eps(itemWeight);
		}

		WeightedSelectorImmutable<T> result = new WeightedSelectorImmutable<T>(offsetToEntry, nextOffset);

		return result;
	}

	@Override
	public Collection<Entry<T, Double>> entries() {
		return offsetToEntry.values();
	}
}