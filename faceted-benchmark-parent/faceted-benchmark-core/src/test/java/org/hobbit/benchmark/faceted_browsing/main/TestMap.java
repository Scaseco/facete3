package org.hobbit.benchmark.faceted_browsing.main;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map;

import org.hobbit.core.mapview.MapViewGson;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;


public class TestMap {
	
	@Test
	public void testAddingToAMapsEntrySet() {
		
		JsonObject o = new JsonObject();
		Map<String, String> map = MapViewGson.createMapViewString(o);
		
		map.put("Hello", "World");
		
		map.entrySet().add(new SimpleEntry<>("Hello", "Fred"));
		map.entrySet().add(new SimpleEntry<>("Bye", "Bob"));
		
		map.keySet().remove("Hello");
		
		{
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				it.next();
				it.remove();
			}
		}
		
		{
			map.entrySet().add(new SimpleEntry<>("Hello", "Fred"));
			map.entrySet().add(new SimpleEntry<>("Bye", "Bob"));

			Iterator<String> it = map.values().iterator();
			it.next();
			it.remove();
		}

		Assert.assertEquals(new ImmutableMap.Builder<String, String>().put("Bye", "Bob").build(), map);
	}
}
