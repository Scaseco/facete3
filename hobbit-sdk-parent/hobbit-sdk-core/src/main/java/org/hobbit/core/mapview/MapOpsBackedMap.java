package org.hobbit.core.mapview;

import java.util.AbstractMap;
import java.util.Set;

public class MapOpsBackedMap<E, K, V>
	extends AbstractMap<K, V>
{
	protected E entity;
	protected SimpleMapOps<E, K, V> ops;
	

	transient protected Set<Entry<K, V>> entrySetView;
	transient protected Set<K> keySetView;
	
	public MapOpsBackedMap(E entity, SimpleMapOps<E, K, V> ops) {
		super();
		this.entity = entity;
		this.ops = ops;

		entrySetView = new MapOpsBackedEntrySet<>(entity, ops);
		keySetView = new MapOpsBackedKeySet<>(entity, ops);
	}
	
	@Override
	public V put(K key, V value) {
		V result = get(key);
		ops.put(entity, key, value);
		return result;
	}

	@Override
	public V get(Object key) {
		V result = ops.get(entity, key);
		return result;
	}
	
	@Override
	public boolean containsKey(Object key) {
		boolean result = ops.containsKey(entity, key);
		return result;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return entrySetView;
	}

	@Override
	public Set<K> keySet() {
		return keySetView;
	}
}
