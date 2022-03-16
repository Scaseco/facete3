package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import org.aksw.jenax.annotation.reprogen.IriNs;

public interface RdfRange {
    @IriNs("eg")
    Integer getMin();

    @IriNs("eg")
    Integer getMax();

    RdfRange setMin(Integer value);
    RdfRange setMax(Integer value);
}
