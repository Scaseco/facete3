package org.hobbit.benchmarks.faceted_benchmark.main;

public interface ServiceFactory {
    ServiceClient createService(String serviceName, Object ... args);
}
