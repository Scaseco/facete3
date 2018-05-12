package org.hobbit.core.mapview;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.common.collect.Iterators;


public class MapOpsBackedKeySet<E, K, V>
	extends AbstractSet<K>
{
	protected E entity;
	protected SimpleMapOps<E, K, V> ops;
	
	public MapOpsBackedKeySet(E entity, SimpleMapOps<E, K, V> ops) {
		super();
		this.entity = entity;
		this.ops = ops;
	}

	@Override
	public boolean contains(Object o) {
		boolean result = ops.containsKey(entity, o);
		return result;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = contains(o);
		if(result) {
			ops.remove(entity, o);
		}
		return result;
	}
	
	@Override
	public Iterator<K> iterator() {
		Iterator<K> result = Iterators.transform(ops.iterator(entity), Entry::getKey);

		return result;
	}

	@Override
	public int size() {
		int result = ops.size(entity);
		return result;
	}
}


