package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import org.aksw.jenax.sparql.fragment.api.Fragment1;

public interface HierarchyCore {
    Fragment1 roots();
    Fragment1 children(Fragment1 nodes);
    Fragment1 parents(Fragment1 nodes);

    Fragment1 descendents();
    Fragment1 ancestors();
}