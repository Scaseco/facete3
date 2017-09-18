package org.hobbit.benchmarks.faceted_browsing.main;

import java.nio.ByteBuffer;

import org.hobbit.benchmarks.faceted_browsing.components.DefaultCommandHandler;
import org.hobbit.benchmarks.faceted_browsing.components.HobbitLocalConfig;
import org.hobbit.core.Commands;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainHobbitFacetedeBrowsingBenchmark {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(HobbitLocalConfig.class);

        DefaultCommandHandler commandHandler = ctx.getBean(DefaultCommandHandler.class);

        commandHandler.accept(ByteBuffer.wrap(new byte[] {Commands.START_BENCHMARK_SIGNAL}));




    }
}
