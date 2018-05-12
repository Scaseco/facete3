package org.hobbit.core.service.docker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public class EnvironmentUtils {

	public static Map<String, String> listToMap(List<String> list) {
		Map<String, String> result = listToMap("=", list);
		return result;
	}
	
	public static Map<String, String> listToMap(String sep, List<String> list) {
        Map<String, String> result = list.stream()
            .map(e -> e.split(sep, 2))
            .collect(Collectors.toMap(
                    e -> e[0],
                    e -> e.length <= 1 ? "" : e[1]));

        return result;
    }

    public static List<String> mapToList(Map<String, String> map) {
    	List<String> result = mapToList("=", map);
    	return result;
    }
    
    public static List<String> mapToList(String sep, Map<String, String> map) {
        List<String> result = map.entrySet().stream()
                .map(e -> e.getKey() + sep + MoreObjects.firstNonNull(e.getValue(), ""))
                .collect(Collectors.toList());

        return result;
    }
}
