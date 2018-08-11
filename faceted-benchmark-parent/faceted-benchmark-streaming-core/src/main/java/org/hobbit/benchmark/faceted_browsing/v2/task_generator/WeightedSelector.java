package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.common.base.Functions;

public class WeightedSelector<T>
	implements Function<Double, T>
{
	protected double totalWeight;
	protected NavigableMap<Double, T> weightToItem;

	public WeightedSelector(double totalWeight,NavigableMap<Double, T> weightToItem) {
		super();
		this.totalWeight = totalWeight;
		this.weightToItem = weightToItem;
	}

	@Override
	public T apply(Double t) {
		double d = t.doubleValue();
		if(d < 0.0 || d > 1.0) {
			throw new IllegalArgumentException("Argument must be in the interval [0, 1]");
		}
		
		double key = t * totalWeight;
		
		T result = weightToItem == null || weightToItem.isEmpty() ? null : weightToItem.floorEntry(key).getValue();
		
		return result;
	}

	public static <T> WeightedSelector<T> create(Iterable<T> items, Function<? super T, ? extends Number> getWeight) {
		return create(items, Functions.identity(), getWeight);
	}

	public static <X, T> WeightedSelector<T> create(Iterable<X> items, Function<? super X, ? extends T> getEntity, Function<? super X, ? extends Number> getWeight) {
		NavigableMap<Double, T> offsetToItem = new TreeMap<>();

		double totalWeight = 0.0;
		for(X item : items) {
			T entity = getEntity.apply(item);
			
			double itemWeight = getWeight.apply(item).doubleValue();
			offsetToItem.put(totalWeight, entity);
		
			totalWeight += itemWeight;
		}
		
		WeightedSelector<T> result = new WeightedSelector<>(totalWeight, offsetToItem);
		return result;
	}	
}