package org.hobbit.core.mapview;

import java.util.Map;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class MapViewGson {
	public static Map<String, String> createMapViewString(JsonObject jsonObj) {
		Converter<String, JsonElement> converter = new Converter<String, JsonElement>() {

			@Override
			protected JsonElement doForward(String a) {
				return new JsonPrimitive(a);
			}

			@Override
			protected String doBackward(JsonElement b) {
				String result;
				if(b.isJsonPrimitive()) {
					JsonPrimitive primitive = b.getAsJsonPrimitive();
					if(primitive.isString()) {
						result = primitive.getAsString();
					} else {
						throw new RuntimeException("String primitive expected, but encountered " + b + " instead");						
					}
				} else {
					throw new RuntimeException("String primitive expected, but encountered " + b + " instead");
				}
				return result;
			}
		};

		SimpleMapOps<JsonObject, String, JsonElement> coreMapOps = new SimpleMapOpsJsonObject();
		SimpleMapOps<JsonObject, String, String> mapOps = new SimpleMapsOpsValueConverting<>(coreMapOps, converter);
		
		Map<String, String> result = new MapOpsBackedMap<>(jsonObj, mapOps);
		return result;
	}
}
