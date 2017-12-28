package org.hobbit.core.mapview;

import java.util.Iterator;
import java.util.Map.Entry;

public interface SimpleMapOps<E, K, V> {
	boolean containsKey(E entity, Object key);
	V get(E entity, Object key);
	void put(E entity, K key, V value);
	void remove(E entity, Object key);

	Iterator<Entry<K, V>> iterator(E entity);
	
	int size(E entity);

	
	/**
	 * Util method which could be used to support removal of items yeld by .iterator() using the MapOps' remove method.
	 * Can be used as a last resort if the iterator returned by .iterator() does not support removals directly. 
	 * Note that this may cause concurrent modification issues.
	 * 
	 * 
	 * @param entity
	 * @param mapOps
	 * @param it
	 * @return
	 */
	public static <E, K, V> Iterator<Entry<K, V>> wrapWithRemoval(E entity, SimpleMapOps<E, K, V> mapOps, Iterator<Entry<K, V>> it) {
		return new RemovingIterator<>(it, e -> mapOps.remove(entity, e.getKey()));
	}
}