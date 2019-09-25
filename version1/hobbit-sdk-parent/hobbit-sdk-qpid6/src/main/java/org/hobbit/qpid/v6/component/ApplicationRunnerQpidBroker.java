package org.hobbit.qpid.v6.component;

import javax.inject.Inject;

import org.apache.qpid.server.Broker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunnerQpidBroker
	implements ApplicationRunner
{
	@Inject
	protected Broker broker;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		broker.startup();
	}
}
