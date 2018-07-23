package org.hobbit.benchmark.faceted_browsing.config;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hobbit.core.Constants;
import org.hobbit.core.config.CommunicationWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class ConfigCommunicationWrapper
	implements EnvironmentAware {
		
	private static final Logger logger = LoggerFactory
			.getLogger(ConfigCommunicationWrapper.class);

	
	protected Environment env;
	
	protected String sessionId;
	protected Set<String> acceptedHeaderIds;

	
	@Bean
	public CommunicationWrapper<ByteBuffer> communicationWrapper() {			
		sessionId = env.getRequiredProperty(Constants.HOBBIT_SESSION_ID_KEY);//, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);
		//sessionId = env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);
		logger.info("SessionId obtained from the environment is: " + sessionId);
		
		acceptedHeaderIds = new LinkedHashSet<>(Arrays.asList(
				sessionId,
				Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS
			));

		CommunicationWrapper<ByteBuffer> result = new CommunicationWrapperSessionId(sessionId, acceptedHeaderIds);
		return result;
	}
	
	@Override
	public void setEnvironment(Environment environment) {
		this.env = environment;
	}
	
}