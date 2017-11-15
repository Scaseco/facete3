package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.function.BiConsumer;

public interface ServiceClient
    extends AutoCloseable //, Invocable
{
    Object invoke(String name, Object ... args);

    void onClose(BiConsumer<String, Integer> serviceNameAndExitCode);
}
