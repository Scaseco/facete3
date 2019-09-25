package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class ConfigDataQueueFactory {
    @Bean
    public DataQueueFactory dataQueueFactory(@Qualifier("queueNameMapper") Function<String, String> queueNameMapper) {
        DataQueueFactory result = new DataQueueFactoryImpl();
        
        if(queueNameMapper != null) {
            result = new DataQueueFactoryRenaming(result, queueNameMapper);
        }
        
        return result;
    }
    
}