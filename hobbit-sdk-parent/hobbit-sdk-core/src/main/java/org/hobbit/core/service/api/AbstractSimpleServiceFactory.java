package org.hobbit.core.service.api;

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
