package org.hobbit.benchmark.faceted_browsing.v2.main;

import com.google.common.collect.Range;

public interface FacetTreeNode {
	/** Whether to retrieve outgoing facets of this node */
	boolean loadOut();
	
	/** Whether to retrieve incoming facets of this node */
	boolean loadIn();
	
	void setLoadRange(Range<Long> range);
}
