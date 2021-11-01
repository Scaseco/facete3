package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import org.aksw.jenax.sparql.relation.api.UnaryRelation;

public interface HierarchyCore {
    UnaryRelation roots();
    UnaryRelation children(UnaryRelation nodes);
    UnaryRelation parents(UnaryRelation nodes);

    UnaryRelation descendents();
    UnaryRelation ancestors();
}