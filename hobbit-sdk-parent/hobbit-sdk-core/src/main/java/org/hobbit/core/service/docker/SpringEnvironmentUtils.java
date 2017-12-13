package org.hobbit.core.service.docker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

public class SpringEnvironmentUtils {
	public static Map<String, Object> toMap(Environment env) {
        Map<String, Object> result = new HashMap<>();
        for(Iterator<?> it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
            PropertySource<?> propertySource = (PropertySource<?>) it.next();
            if (propertySource instanceof MapPropertySource) {
                result.putAll(((MapPropertySource) propertySource).getSource());
            }
        }
        
        return result;
	}

	public static Map<String, String> toStringMap(Environment env) {
		Map<String, Object> tmp = toMap(env);
		Map<String, String> result = toStringMap(tmp);
		return result;
	}

	public static Map<String, String> toStringMap(Map<String, ?> map) {
		Map<String, String> result = map.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> Objects.toString(e.getValue())));
		return result;
	}
}
