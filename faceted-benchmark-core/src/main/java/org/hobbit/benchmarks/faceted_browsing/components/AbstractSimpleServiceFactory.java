package org.hobbit.benchmarks.faceted_browsing.components;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.util.concurrent.Service;

public abstract class AbstractSimpleServiceFactory<T extends Service>
    implements SimpleServiceFactory<T>
{
    protected Map<String, String> environment = new LinkedHashMap<>();

    public Map<String, String> getEnvironment() {
        return environment;
    }
}
