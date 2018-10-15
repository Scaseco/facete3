package org.aksw.facete.v3.bgp.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface BgpMultiNode
	extends Resource
{
	BgpNode parent();
	
	Property reachingProperty();
	
	@Deprecated // use isForard only
	boolean isReverse();

	default boolean isForward() {
		return !isReverse();
	}
	
	/**
	 * getOrCreate the one single alias - and marks it as the default -
	 * for this multi node. Raises an exception if multiple default aliases exist
	 */
	BgpNode one();
	boolean contains(BgpNode bgpNode);
}
