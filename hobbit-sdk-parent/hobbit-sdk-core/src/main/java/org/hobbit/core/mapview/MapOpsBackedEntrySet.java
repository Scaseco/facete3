package org.hobbit.core.mapview;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.apache.jena.ext.com.google.common.base.Objects;

public class MapOpsBackedEntrySet<E, K, V>
	extends AbstractSet<Entry<K, V>>
{
	protected E entity;
	protected SimpleMapOps<E, K, V> ops;
	
	public MapOpsBackedEntrySet(E entity, SimpleMapOps<E, K, V> ops) {
		super();
		this.entity = entity;
		this.ops = ops;
	}

	@Override
	public boolean add(Entry<K, V> e) {
		boolean result = false;
		if(!contains(e)) {
			ops.put(entity, e.getKey(), e.getValue());
		}
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	protected Entry<K, V> getEntry(Object o) {
		
		Entry<K, V> result = null;
		if(o instanceof Entry) {
			Entry<?, ?> e = (Entry<?, ?>)o;
			Object k = e.getKey();
			Object v = e.getValue();
			
			if(ops.containsKey(entity, k)) {
				V x = ops.get(entity, k);
				if(Objects.equal(v, x)) {
					result = new SimpleEntry<>((K)k, x);
				}
			}
		}
		return result;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = false;
		Entry<K, V> e = getEntry(o);
		if(e != null) {
			ops.remove(entity, e.getKey());
			result = true;
		}
		return result;
	}
	
	@Override
	public boolean contains(Object o) {
		boolean result = false;

		if(o instanceof Entry) {
			Entry<?, ?> e = (Entry<?, ?>)o;
			Object k = e.getKey();
			Object v = e.getValue();
			
			if(ops.containsKey(entity, k)) {
				V x = ops.get(entity, k);
				result = Objects.equal(v, x);
			}
		}
		return result;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator() {
//		Iterator<Entry<K, V>> result = Streams.stream(ops.keys(entity))
//			.map(key -> (Entry<K, V>)new SimpleEntry<>(key, ops.get(entity, key)))
//			.iterator();

		Iterator<Entry<K, V>> result = ops.iterator(entity);
		return result;
	}

	@Override
	public int size() {
		int result = ops.size(entity);
		return result;
	}
}