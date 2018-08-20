package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import org.apache.jena.rdf.model.Resource;

public interface SetSummary
	extends Resource
{
	Long getTotalValueCount();
	Long getDistinctValueCount();
	Number getMin();
	Number getMax();
}
