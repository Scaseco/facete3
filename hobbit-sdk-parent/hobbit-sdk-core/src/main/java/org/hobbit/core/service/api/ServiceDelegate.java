package org.hobbit.core.service.api;

import com.google.common.util.concurrent.Service;

public interface ServiceDelegate<T>
	extends Service
{
	// The entity being delegated to - may be null
	T getEntity();
}
