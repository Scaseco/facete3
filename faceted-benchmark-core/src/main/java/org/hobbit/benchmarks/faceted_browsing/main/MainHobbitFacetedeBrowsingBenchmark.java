package org.hobbit.benchmarks.faceted_browsing.main;

import java.nio.ByteBuffer;

import org.hobbit.benchmarks.faceted_browsing.components.PseudoHobbitPlatformController;
import org.hobbit.config.local.ConfigHobbitLocalChannels;
import org.hobbit.config.local.ConfigHobbitLocalPlatformFacetedBenchmark;
import org.hobbit.config.local.ConfigHobbitLocalServices;
import org.hobbit.core.Commands;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainHobbitFacetedeBrowsingBenchmark {

    public static void main(String[] args) {

        try(AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ConfigHobbitLocalPlatformFacetedBenchmark.class,
                ConfigHobbitLocalChannels.class,
                ConfigHobbitLocalServices.class)) {

            PseudoHobbitPlatformController commandHandler = ctx.getBean(PseudoHobbitPlatformController.class);
            commandHandler.accept(ByteBuffer.wrap(new byte[] {Commands.START_BENCHMARK_SIGNAL}));
        }

    }
}
