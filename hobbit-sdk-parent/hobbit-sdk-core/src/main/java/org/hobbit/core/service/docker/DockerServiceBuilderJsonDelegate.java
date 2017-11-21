package org.hobbit.core.service.docker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Delegates the creation of a docker container to the provided
 * start / stop and run functions.
 *
 *
 * FIXME How to determine whether the service is running / terminated?
 * - One option could be that there has to be a run function which blocks as long as the remote service is running.
 *   For instance, the run function could wait for the termination signal from the platform
 *   Downside: The service needs an extra thread just to wait
 * - Another option: A handler outside of the service receives the signals and calls stop
 *   This is the way to go
 *
 * @author raven Sep 24, 2017
 *
 */
public class DockerServiceBuilderJsonDelegate<T extends DockerService>
    implements DockerServiceBuilder<T>//, Cloneable
{
	public static final String KEY_IMAGE_NAME = "imageName";
	public static final String KEY_ENV = "env";
	
	protected JsonObject config;
	
	protected Function<JsonObject, T> delegate;
	
    public DockerServiceBuilderJsonDelegate(Function<JsonObject, T> delegate) {
    	this(delegate, new JsonObject());
    }

    public DockerServiceBuilderJsonDelegate(Function<JsonObject, T> delegate, JsonObject config) {
        super();
        this.delegate = delegate;
        this.config = config;
    }

    @Override
    public String getImageName() {
        String result = config.get(KEY_IMAGE_NAME).getAsString();
        return result;
    }

    @Override
    public DockerServiceBuilderJsonDelegate<T> setImageName(String imageName) {
    	config.remove(KEY_IMAGE_NAME);
    	config.addProperty(KEY_IMAGE_NAME, imageName);
    	
    	return this;
    }

    @Override
    public Map<String, String> getLocalEnvironment() {
    	// TODO Map wrapper around a json object...
    	return new HashMap<>();
    }

    @Override
    public DockerServiceBuilderJsonDelegate<T> setLocalEnvironment(Map<String, String> environment) {
    	Gson gson = new Gson();
    	JsonElement json = gson.toJsonTree(environment);
    	
    	config.remove(KEY_ENV);
    	config.add(KEY_ENV, json);
    	//throw new UnsupportedOperationException();
    	return this;
    }

    @Override
    public T get() {
    	T result = delegate.apply(config);
        return result;
    }

    
    public static <T extends DockerService> DockerServiceBuilder<T> create(BiFunction<String, Map<String, String>, T> serviceFactory) {
    	Function<JsonObject, T> wrapperFn = jsonObj -> {
    		String imageName = jsonObj.get(KEY_IMAGE_NAME).getAsString();
    		Map<String, String> env = new HashMap<>();
    		
    		T r = serviceFactory.apply(imageName, env);
    		return r;
    	};
    	
    	DockerServiceBuilderJsonDelegate<T> result = new DockerServiceBuilderJsonDelegate<>(wrapperFn);
    	return result;
    }

}
