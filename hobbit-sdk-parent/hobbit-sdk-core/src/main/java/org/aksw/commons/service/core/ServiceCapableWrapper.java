package org.aksw.commons.service.core;

import org.hobbit.core.service.api.ExecutionThreadServiceDelegate;
import org.hobbit.core.service.api.IdleServiceCapable;
import org.hobbit.core.service.api.IdleServiceDelegate;
import org.hobbit.core.service.api.RunnableServiceCapable;
import org.hobbit.core.service.api.ServiceCapable;
import org.hobbit.core.service.api.ServiceDelegate;

import com.google.common.util.concurrent.Service;

public class ServiceCapableWrapper {
	public static Service wrapService(Object obj) {
		Service result = obj instanceof Service
				? (Service)obj
				: obj instanceof ServiceCapable
					? wrap((ServiceCapable)obj)
					: null;
		
		if(result == null) {
            throw new RuntimeException("Could not determine how to wrap the component as a service: " + obj.getClass());			
		}

        return result;
	}

	//<T extends ServiceCapable> 
	public static ServiceDelegate<? extends ServiceCapable> wrap(Object obj) {
		ServiceDelegate<? extends ServiceCapable> result;
		
        // Determine the appropriate service wrapper for the component
        if(obj instanceof IdleServiceCapable) {
        	IdleServiceCapable tmp = (IdleServiceCapable)obj;
            result = new IdleServiceDelegate<IdleServiceCapable>(tmp);
        } else if(obj instanceof RunnableServiceCapable) {
            RunnableServiceCapable tmp = (RunnableServiceCapable)obj;
            //componentService = new ExecutionThreadServiceDelegate(tmp::startUp, tmp::run, tmp::shutDown);
            result = new ExecutionThreadServiceDelegate<>(tmp);
        } else {
            throw new RuntimeException("Could not determine how to wrap the component as a service: " + obj.getClass());
        }

        return result;
	}
}
