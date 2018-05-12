package org.hobbit.core.service.api;

import com.google.common.util.concurrent.Service;

public abstract class DockerApiService<S extends Service, A>
	extends ServiceDelegate<S>
{
	public DockerApiService(S delegate) {
		super(delegate);
	}

	public abstract A getApi();
}
