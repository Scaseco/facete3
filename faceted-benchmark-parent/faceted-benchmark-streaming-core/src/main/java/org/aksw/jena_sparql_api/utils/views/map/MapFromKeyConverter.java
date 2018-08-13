package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Converter;

public class MapFromKeyConverter<K, J, V>
	extends AbstractMap<K, V>
{
	protected Map<J, V> map;
	protected Converter<J, K> converter;

	@Override
	public V put(K key, V value) {
		J j = converter.reverse().convert(key);
		V result = map.put(j, value);
		return result;
	}
	
	@Override
	public boolean containsKey(Object key) {
		J j = converter.reverse().convert((K)key);
		boolean result = map.containsKey(j);
		return result;
	}
	
	@Override
	public V remove(Object key) {
		J j = converter.reverse().convert((K)key);
		V result = map.remove(j);
		return result;
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
//		new SetFromCollection()
		return null;
	}
}
