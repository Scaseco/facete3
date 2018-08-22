package org.hobbit.core.component;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AbstractService;

/**
 * A no-op service with a 'MainService' qualifier.
 * Can be used together with our ServiceSpringApplicationBuilder to tie life cycle management of the
 * context to a no-op service bean. (A SpringApplicationBuilder requires the definition of a
 * bean that runs the application; Our <b>Service</b>SpringApplicationBuilder wraps a SpringApplicationBuilder
 * and assumes a guava Service with the spring qualifier "MainService" exists and bidirectly
 * ties the life cycle management of that service bean and the context together.
 * With a ServiceNoOp, the context is only destroyed when the service is manually stopped.
 * 
 * <pre>
 * {@code
 * 
 * Service amqpService = new ServiceSpringApplicationBuilder("qpid-server", new SpringApplicationBuilder()
 *     .sources(ConfigQpidBroker.class)
 *     .sources(ServiceNoOp.class))
 * }
 * </pre>
 * 
 * 
 * TODO Move to some generic toolkit
 * 
 * @author raven
 *
 */
@Component
@Qualifier("MainService")
public class ServiceNoOp
	extends AbstractService
{
	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}
}
