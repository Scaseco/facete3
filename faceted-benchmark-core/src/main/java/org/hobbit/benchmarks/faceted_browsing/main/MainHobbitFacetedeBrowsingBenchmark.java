package org.hobbit.benchmarks.faceted_browsing.main;

import java.nio.ByteBuffer;

import org.hobbit.benchmarks.faceted_browsing.components.PseudoHobbitPlatformController;
import org.hobbit.benchmarks.faceted_browsing.components.HobbitLocalConfig;
import org.hobbit.core.Commands;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainHobbitFacetedeBrowsingBenchmark {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(HobbitLocalConfig.class);

        PseudoHobbitPlatformController commandHandler = ctx.getBean(PseudoHobbitPlatformController.class);

        commandHandler.accept(ByteBuffer.wrap(new byte[] {Commands.START_BENCHMARK_SIGNAL}));




    }
}
