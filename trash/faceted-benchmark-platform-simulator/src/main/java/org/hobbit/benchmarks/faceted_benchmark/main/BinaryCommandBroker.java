package org.hobbit.benchmarks.faceted_benchmark.main;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;

public interface BinaryCommandBroker {
    public <T> void registerStreamingCommand(String name, Function<List<?>, OutputStream> callable);
    public InputStream executeStreamingCommand(String name, Object ... args);
}
