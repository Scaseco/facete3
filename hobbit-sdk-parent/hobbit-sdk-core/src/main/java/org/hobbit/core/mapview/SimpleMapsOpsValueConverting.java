package org.hobbit.core.mapview;

import java.util.Iterator;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import com.google.common.base.Converter;
import com.google.common.collect.Streams;

import jersey.repackaged.com.google.common.collect.Iterators;

public class SimpleMapsOpsValueConverting<E, K, V, W>
	implements SimpleMapOps<E, K, V>
{
	protected SimpleMapOps<E, K, W> delegate;
	protected Converter<V, W> converter;
	
	
	public SimpleMapsOpsValueConverting(SimpleMapOps<E, K, W> delegate, Converter<V, W> converter) {
		super();
		this.delegate = delegate;
		this.converter = converter;
	}

	@Override
	public boolean containsKey(E entity, Object key) {
		boolean result = delegate.containsKey(entity, key);
		return result;
	}

	@Override
	public V get(E entity, Object key) {
		V result = null;
		if(delegate.containsKey(entity, key)) {
			W raw = delegate.get(entity, key);
			result = converter.reverse().convert(raw);
		}
		return result;
	}

	@Override
	public void put(E entity, K key, V value) {
		W raw = converter.convert(value);
		delegate.put(entity, key, raw);
	}

	@Override
	public void remove(E entity, Object key) {
		delegate.remove(entity, key);
	}

	@Override
	public Iterator<Entry<K, V>> iterator(E entity) {
		Iterator<Entry<K, V>> result = Iterators.transform(delegate.iterator(entity),
				e -> (Entry<K, V>)new SimpleEntry<>(e.getKey(), converter.reverse().convert(e.getValue())));

		return result;
	}

	@Override
	public int size(E entity) {
		int result = delegate.size(entity);
		return result;
	}
	
}