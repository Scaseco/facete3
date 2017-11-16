package org.hobbit.core.service.api;

import java.util.Map;

import com.google.common.util.concurrent.Service;

/**
 * A simple service factory is configured via an environment
 *
 * @author raven
 *
 */
public interface SimpleServiceFactory<T extends Service>
    extends ServiceFactory<T>
{
    Map<String, String> getEnvironment();
}
