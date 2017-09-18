package org.hobbit.benchmarks.faceted_browsing.components;

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.Service;

/**
 * Tag interface for dependency injection
 *
 * @author raven
 *
 */
public interface ServiceFactory<T extends Service>
    extends Supplier<T>
{
}
