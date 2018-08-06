package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import org.aksw.jena_sparql_api.concepts.UnaryRelation;

public interface HierarchyCore {
	UnaryRelation roots();
	UnaryRelation children(UnaryRelation nodes);
	UnaryRelation parents(UnaryRelation nodes);

	UnaryRelation descendents();
	UnaryRelation ancestors();
}