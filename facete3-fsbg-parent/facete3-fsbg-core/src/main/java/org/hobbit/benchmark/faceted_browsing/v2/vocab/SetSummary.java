package org.hobbit.benchmark.faceted_browsing.v2.vocab;

import org.apache.jena.rdf.model.Resource;

/**
 * Statistics characterizing a bag of (numeric) items.
 * 
 * 
 * @author Claus Stadler, Oct 9, 2018
 *
 */
public interface SetSummary
	extends Resource
{
	Long getTotalValueCount();
	Long getDistinctValueCount();
	Number getMin();
	Number getMax();
}
