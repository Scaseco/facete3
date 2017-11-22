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

	public static <T extends ServiceCapable> ServiceDelegate<T> wrap(T obj) {
		ServiceDelegate<T> result;
		
        // Determine the appropriate service wrapper for the component
        if(obj instanceof IdleServiceCapable) {
            IdleServiceCapable tmp = (IdleServiceCapable)obj;
            result = new IdleServiceDelegate<>(
            		obj,
                    () -> { try { tmp.startUp(); } catch(Exception e) { throw new RuntimeException(e); }},
                    () -> { try { tmp.shutDown(); } catch(Exception e) { throw new RuntimeException(e); }});


        } else if(obj instanceof RunnableServiceCapable) {
            RunnableServiceCapable tmp = (RunnableServiceCapable)obj;
            //componentService = new ExecutionThreadServiceDelegate(tmp::startUp, tmp::run, tmp::shutDown);
            result = new ExecutionThreadServiceDelegate<>(
            		obj,
                    () -> { try { tmp.startUp(); } catch(Exception e) { throw new RuntimeException(e); }},
                    () -> { try { tmp.run(); } catch(Exception e) { throw new RuntimeException(e); }},
                    () -> { try { tmp.shutDown(); } catch(Exception e) { throw new RuntimeException(e); }});
        } else {
            throw new RuntimeException("Could not determine how to wrap the component as a service: " + obj.getClass());
        }

        return result;
	}
}
