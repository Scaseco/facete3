package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface CommandBroker {
//    public void registerCommand(String name, Function<List<?>, Object> callable);
//    public Object executeCommand();


    public <T> void registerStreamingCommand(String name, Function<List<?>, Stream<T>> callable);
    public <T> Stream<T> executeStreamingCommand(String name, Object ... args);
}
