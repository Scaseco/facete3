package trash.org.hobbit.faceted_browsing.quad_tree;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author Claus Stadler, Jul 19, 2018
 *
 * @param <E> The envelope type
 */
public interface QuadTreeNode<E> {
	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;

	QuadTreeNode<E> getParent();
	int getDepth();
	
	Envelope getEnvelope();
}
