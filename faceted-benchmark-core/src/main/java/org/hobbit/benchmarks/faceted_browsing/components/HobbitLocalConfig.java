package org.hobbit.benchmarks.faceted_browsing.components;

import org.springframework.context.annotation.Bean;


public class HobbitLocalConfig {
    @Bean
    public ObservableByteChannel cmdQueue() {
        return new ObservableByteChannel();
    }


    /**
     * Register the default command handler
     * @param cmdQueue
     * @return
     */
    @Bean
    public DefaultCommandHandler defaultCommandHandler(ObservableByteChannel cmdQueue) {
        DefaultCommandHandler result = new DefaultCommandHandler(BenchmarkControllerFacetedBrowsing.class);
        cmdQueue.addObserver(result);
        return result;
    }
}

