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
    public PseudoHobbitPlatformController defaultCommandHandler(ObservableByteChannel cmdQueue) {
        PseudoHobbitPlatformController result = new PseudoHobbitPlatformController(BenchmarkControllerFacetedBrowsing.class);
        cmdQueue.addObserver(result);
        return result;
    }
}

