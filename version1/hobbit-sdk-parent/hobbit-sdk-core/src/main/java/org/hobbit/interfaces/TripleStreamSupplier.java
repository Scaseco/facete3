package org.hobbit.interfaces;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;

public interface TripleStreamSupplier
    extends Supplier<Stream<Triple>>
{
}
