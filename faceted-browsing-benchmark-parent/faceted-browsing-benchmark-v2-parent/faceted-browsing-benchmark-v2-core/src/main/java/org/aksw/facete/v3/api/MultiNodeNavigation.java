package org.aksw.facete.v3.api;

/**
 * Interface for MultiNodes
 */
public interface MultiNodeNavigation<N> {
	/** getOrCreate the one single alias for this multi node. Raises an exception if there are already multiple aliases */
	N one();

}
