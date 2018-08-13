package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.common.base.Functions;
import com.google.common.collect.Streams;

import jersey.repackaged.com.google.common.collect.Maps;

public class WeightedSelector<T>
//	implements Function<Double, T>
{
	protected double nextOffset;

	protected NavigableMap<Double, Entry<T, Double>> offsetToEntry;
	protected Map<Entry<T, Double>, Double> entryToOffset;	
	protected Map<T, Entry<T, Double>> itemToEntry;
	

	public WeightedSelector() {
		super();
		this.nextOffset = 0.0;
		offsetToEntry = new TreeMap<>();
		entryToOffset = new HashMap<>();		
		itemToEntry = new HashMap<>();
	}

	//@Override
	public Entry<T, Double> sample(Double t) {
		double d = Objects.requireNonNull(t).doubleValue();
		if(d < 0.0 || d > 1.0) {
			throw new IllegalArgumentException("Argument must be in the interval [0, 1]");
		}
		
		double key = t * nextOffset;
		
		Entry<T, Double> result = offsetToEntry == null || offsetToEntry.isEmpty() ? null : offsetToEntry.floorEntry(key).getValue();
		
		return result;
	}
	
//	public Optional<Double> sampleValue(Double t) {
//		return Optional.ofNullable(sample(t)).map(Entry::getValue);
//	}

	public T sampleValue(Double t) {
		return Optional.ofNullable(sample(t)).map(Entry::getKey).orElse(null);
	}

	public Double getWeight(T item) {
		Entry<T, Double> e = itemToEntry.get(item);
		return e == null ? null : e.getValue();
	}
	
	public void setWeight(T item, double weight) {
		Entry<T, Double> e = itemToEntry.get(item);
		if(e != null) {
			remove(e);
		}

		put(item, weight);
	}
	
	
	public void removeAll(Collection<Entry<T, Double>> es) {
		// TODO Find the lowest offset, and only re-index from there
		
		for(Entry<T, Double> e : es) {
			remove(e);
		}
	}
	
	public void remove(Entry<T, Double> e) {
		Double offset = entryToOffset.get(e);
		if(offset != null) {			
			Collection<Entry<T, Double>> tailMap = offsetToEntry.tailMap(offset).values();
			List<Entry<T, Double>> reinserts = new ArrayList<>(tailMap.size() - 1);
			
			// Remove the first entry, and collect the remaining ones for re-indexing
			boolean isFirst = true;
			
			Iterator<Entry<T, Double>> it = tailMap.iterator();

			// Remove remaining items
			while(it.hasNext()) {
				Entry<T, Double> f = it.next();

				if(isFirst) {
					isFirst = false;
				} else {
					reinserts.add(f);
				}
				
				it.remove();
				itemToEntry.remove(f.getKey());
				entryToOffset.remove(f);				
			}
			
			nextOffset = offset;
			for(Entry<T, Double> i : reinserts) {
				add(i);
			}
			
		}		
	}
	
	/**
	 * Adds a new item with the given weight.
	 * If the item already exists, the weight is added
	 * 
	 * @param e
	 */
	public void add(Entry<T, Double> e) {
		Objects.requireNonNull(e);

		double weight = e.getValue().doubleValue();
		if(weight < 0.0) {
			throw new IllegalArgumentException("Weight must be positive");
		}

		if(weight > 0.0) {
			T item = e.getKey();
			Entry<T, Double> priorEntry = itemToEntry.get(item);
			if(priorEntry != null) {
				double priorWeight = priorEntry.getValue();
				weight = priorWeight + e.getValue();
				e = Maps.immutableEntry(item, weight);
			}
			
			offsetToEntry.put(nextOffset, e);
			entryToOffset.put(e, nextOffset);
			itemToEntry.put(item, e);
			
			nextOffset += weight;
		}
	}
	
	public void put(T item, Double weight) {
		add(Maps.immutableEntry(item, weight));
	}

	public static <T> WeightedSelector<T> create(Map<T, ? extends Number> map, Function<? super T, ? extends Number> getWeight) {
		return create(map.entrySet(), Entry::getKey, Entry::getValue);
	}

	public static <T> WeightedSelector<T> create(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		return create(items, Functions.identity(), getWeight);
	}

	public static <X, T> WeightedSelector<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		WeightedSelector<T> result = new WeightedSelector<T>();
		
		for(X item : items) {
			T entity = getEntity.apply(item);
			double itemWeight = getWeight.apply(item).doubleValue();
			result.put(entity, itemWeight);
		}

		return result;
	}
	
	

	// Based on https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
	// May also EnumeratedDistribution
	public static <T> T chooseRandomItem(Random rand, Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		double totalWeight = getTotalWeight(items, getWeight);
        double r = rand.nextDouble() * totalWeight;
		T result = chooseItem(r, items, getWeight);
			
        return result;
    }
	
	public static <T> double getTotalWeight(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		double result = Streams.stream(items).mapToDouble(x -> getWeight.apply(x).doubleValue()).sum();	
		return result;
	}

	public static <T> T chooseItem(double score, Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		T result = null;

        double countWeight = 0.0;
        for (T item : items) {
            countWeight += getWeight.apply(item).doubleValue();
            if (countWeight >= score) {
            	result = item;
            	break;
            }
        }
        
        return result;
    }
}