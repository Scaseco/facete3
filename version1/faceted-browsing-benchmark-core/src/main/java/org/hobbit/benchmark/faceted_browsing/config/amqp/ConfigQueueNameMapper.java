package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.util.function.Function;

import org.hobbit.core.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class ConfigQueueNameMapper {	    
    @Bean
    public Function<String, String> queueNameMapper(@Value("${" + Constants.HOBBIT_SESSION_ID_KEY + ":" + Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS + "}") String hobbitSessionId) {
        return queueName -> queueName + "." + hobbitSessionId;
    }
}