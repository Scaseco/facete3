package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Streams;

import com.google.common.base.Functions;

import jersey.repackaged.com.google.common.collect.Maps;

public class WeightedSelectorMutable<T>
	implements WeightedSelector<T>
{
	protected List<Entry<T, Double>> entries;
	protected double nextOffset;

	public WeightedSelectorMutable<T> clone() {
		return new WeightedSelectorMutable<T>(new ArrayList<>(entries));
	}

	public static double eps(double d) {
		double e = 0.00001;
		return d >= 0.0 && d < e ? e : d;
	}
	
	public WeightedSelectorMutable() {
		this(new ArrayList<>());
	}

	public WeightedSelectorMutable(List<Entry<T, Double>> entries) {
		this.entries = entries;
		this.nextOffset = entries.stream().mapToDouble(Entry::getValue).sum();
	}
	
	public Entry<Integer, Entry<T, Double>> sampleEntryWithIndex(Double t) {
		double d = Objects.requireNonNull(t).doubleValue();
		if(d < 0.0 || d > 1.0) {
			throw new IllegalArgumentException("Argument must be in the interval [0, 1]");
		}
		
		double key = t * nextOffset;
		
		double current = 0.0;
		Entry<T, Double> match = null;
		
		int i = -1;
		for(Entry<T, Double> e : entries) {
			// The result is the entry whose offset weight is smaller yet closest to key  
			match = e;
			++i;

			double next = current + eps(e.getValue());
			if(key < next) {
				break;
			}
			current = next;
		}

		Entry<Integer, Entry<T, Double>> result = match == null
				? null : Maps.immutableEntry(i, match);
		
		return result;
	}

	@Override
	public Entry<T, Double> sampleEntry(Double t) {
		Entry<Integer, Entry<T, Double>> e = sampleEntryWithIndex(t);
		Entry<T, Double> result = e == null ? null : e.getValue();
		return result;
	}

	@Override
	public Collection<Entry<T, Double>> entries() {
		return entries;
	}
	
	public void put(T item, double weight) {
		put(Maps.immutableEntry(item, weight));
	}
	
	public void put(Entry<T, Double> e) {
		entries.add(e);
		nextOffset += eps(e.getValue());
	}

	public void putAll(Collection<? extends Entry<T, Double>> items) {
		for(Entry<T, Double> e : items) {
			put(e);
		}
	}

	public boolean remove(Entry<T, Double> e) {
		boolean result = entries.removeIf(item -> Objects.equals(item, e));
		return result;
	}
	
	
	public static <T> WeightedSelectorMutable<T> create(Map<T, ? extends Number> map) {
		return create(map.entrySet(), Entry::getKey, Entry::getValue);
	}

	public static <T> WeightedSelectorMutable<T> create(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		return create(items, Functions.identity(), getWeight);
	}

//	public static <X, T> WeightedSelectorMutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
//		List<Entry<T, Double>> es = Streams.stream(items)
//				.map(item -> Maps.<T, Double>immutableEntry(getEntity.apply(item), getWeight.apply(item).doubleValue())).
//				collect(Collectors.toCollection(ArrayList::new));
//		
//		return new WeightedSelectorMutable<>(es);
//	}

	public static <X, T> WeightedSelectorMutable<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		return configure((Function<List<Entry<T, Double>>, WeightedSelectorMutable<T>>)WeightedSelectorMutable::new, items, getEntity, getWeight);
	}
	
	public static <X, T, S> S configure(Function<List<Entry<T, Double>>, S> ctor, Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		List<Entry<T, Double>> es = Streams.stream(items)
				.map(item -> Maps.<T, Double>immutableEntry(getEntity.apply(item), getWeight.apply(item).doubleValue())).
				collect(Collectors.toCollection(ArrayList::new));
		
		return ctor.apply(es);
	}
}
