package org.hobbit.core.mapview;

import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SimpleMapOpsJsonObject
	implements SimpleMapOps<JsonObject, String, JsonElement>
{

	@Override
	public boolean containsKey(JsonObject entity, Object key) {
		boolean result = key instanceof String && entity.has((String)key);
		return result;
	}

	@Override
	public JsonElement get(JsonObject entity, Object key) {
		JsonElement result = key instanceof String ? entity.get((String)key) : null;
		return result;
	}

	@Override
	public void put(JsonObject entity, String key, JsonElement value) {
		entity.add(key, value);
	}

	@Override
	public void remove(JsonObject entity, Object key) {
		if(key instanceof String) {
			entity.remove((String)key);
		}
	}

	@Override
	public Iterator<Entry<String, JsonElement>> iterator(JsonObject entity) {
		
		Iterator<Entry<String, JsonElement>> result = entity.entrySet().iterator();
		
		//result = SimpleMapOps.wrapWithRemoval(entity, this, result);

		return result;
	}

	@Override
	public int size(JsonObject entity) {
		int result = entity.size();
		return result;
	}
	
	
}