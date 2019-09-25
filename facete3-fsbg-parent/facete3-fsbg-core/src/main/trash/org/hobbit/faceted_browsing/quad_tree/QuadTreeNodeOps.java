package trash.org.hobbit.faceted_browsing.quad_tree;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public interface QuadTreeNodeOps<N> {
	int getDepth(N node);
	int getMaxDepth(N node);
	
	/**
	 * Subdivide a node - only allowed if hasChildren() yields false
	 * 
	 * @param node
	 */
	void subdivide(N node);
	
	Envelope getEnvelope(N node);
	
	default Coordinate getCenter(N node) {
		Envelope env = getEnvelope(node);
		Coordinate result = env.centre();
		return result;
	}
	
	/**
	 * Return the children in the order
	 * TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT.
	 * 
	 * @return
	 */
	List<N> getChildren(N node);

	default boolean hasChildren(N node) {
		List<N> children = getChildren(node);
		boolean  result = children != null &&  !children.isEmpty();
		return result;
	}
	
	// Low level factory function - should not be used directly
	N createChildNode(N parent, Envelope child);
}
