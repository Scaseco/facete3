package org.hobbit.faceted_browsing.quad_tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class QuadTreeOps {

	/**
	 * Loose quad tree sub division supported by k parameter in the range [0, 1)
	 * Use k = 0 for traditional quad tree (non overlapping child envelopes)
	 * 
	 * @param env
	 * @param k
	 * @return
	 */
	public static List<Envelope> subdivide(Envelope env, double k) {
		Coordinate c = env.centre();
	    // expansions
		double l = 1.0 + k;
	    double ew = l * 0.5 * env.getWidth();
	    double eh = l * 0.5 * env.getHeight();
	
	    List<Envelope> result = new ArrayList<>(4);
	    
	    //QuadTreeNode.TOP_LEFT
	    result.add(new Envelope(
	    	new Coordinate(env.getMinX(), c.y - eh),
	    	new Coordinate(c.x + ew, env.getMaxY())
	    ));
	    //this._maxDepth, depth, this._k, QuadTreeNode.TOP_LEFT);
	
	    // QuadTreeNode.TOP_RIGHT
	    result.add(new Envelope(
	    	new Coordinate(c.x - ew, c.y - eh),
	        new Coordinate(env.getMaxX(), env.getMaxY())
	    ));
	    //this._maxDepth, depth, this._k, QuadTreeNode.TOP_RIGHT);
	
	    // QuadTreeNode.BOTTOM_LEFT
	    result.add(new Envelope(
	    	new Coordinate(env.getMinX(), env.getMinY()),
	    	new Coordinate(c.x + ew, c.y + eh)
	    ));
	    //this._maxDepth, depth, this._k, QuadTreeNode.BOTTOM_LEFT);

	    // QuadTreeNode.BOTTOM_RIGHT
	    result.add(new Envelope(
	    	new Coordinate(c.x - ew, env.getMinY()),
	        new Coordinate(env.getMaxX(), c.y + eh)
	    ));
	    
	    return result;
	}

	
//	static <N> List<N> subdivide(QuadTreeNodeOps<N> ops, N node, double k) {
//		int depth = ops.getDepth(node) + 1;
//
//		Envelope env = ops.getEnvelope(node);
//		Coordinate c = env.centre();
//
//	    // expansions
//	    double ew = k * 0.5 * env.getWidth();
//	    double eh = k * 0.5 * env.getHeight();
//	
//	    List<N> children = new ArrayList<N>(4);
//	    
//	    //QuadTreeNode.TOP_LEFT
//	    children.add(ops.createChildNode(node, new Envelope(
//	        env.getMinX(),
//	        c.y - eh,
//	        c.x + ew,
//	        env.getMaxY()
//	    )));
//	    //this._maxDepth, depth, this._k, QuadTreeNode.TOP_LEFT);
//	
//	    // QuadTreeNode.TOP_RIGHT
//	    children.add(ops.createChildNode(node, new Envelope(
//	        c.x - ew,
//	        c.y - eh,
//	        env.getMaxX(),
//	        env.getMaxY()
//	    )));
//	    //this._maxDepth, depth, this._k, QuadTreeNode.TOP_RIGHT);
//	
//	    // QuadTreeNode.BOTTOM_LEFT
//	    children.add(ops.createChildNode(node, new Envelope(
//	        env.getMinX(),
//	        env.getMinY(),
//	        c.x + ew,
//	        c.y + eh
//	    )));
//	    //this._maxDepth, depth, this._k, QuadTreeNode.BOTTOM_LEFT);
//	
//	    
//	    // QuadTreeNode.BOTTOM_RIGHT
//	    children.add(ops.createChildNode(node, new Envelope(
//	        c.x - ew,
//	        env.getMinY(),
//	        env.getMaxX(),
//	        c.y + eh
//	    )));
//	    //this._maxDepth, depth, this._k, QuadTreeNode.BOTTOM_RIGHT);
//	
//	}



	/**
	 * Return loaded and leaf nodes within the bounds
	 *
	 * @param bounds
	 * @param depth The maximum number of levels to go beyond the level derived from the size of bounds
	 * @returns {Array}
	 */
	public static <N> List<N> query(QuadTreeNodeOps<N> ops, N node, Predicate<? super N> isTerminal, Envelope env, int depth) {
	    List<N> result = new ArrayList<>();
	
	    queryRec(ops, node, isTerminal, env, result, depth);
	
	    return result;
	}

	public static <N> void queryRec(QuadTreeNodeOps<N> ops, N node, Predicate<? super N> isTerminal, Envelope env, List<N> result, int depth) {
	    Envelope nodeEnv = ops.getEnvelope(node);
		if(!nodeEnv.contains(env)) {
	        return;
	    }
	
	    double w = env.getWidth() / nodeEnv.getWidth();
	    double h = env.getHeight() / nodeEnv.getHeight();
	
	    double r = Math.max(w, h);
	
	    // Stop recursion on encounter of a loaded node or leaf node or node that exceeded the depth limit
	    boolean terminal = isTerminal.test(node);
	    if(terminal || !ops.hasChildren(node) || r >= depth) {
	        result.add(node);
	        return;
	    }
	
	    List<N> children = ops.getChildren(node);
	    for(N child : children) {
	        queryRec(ops, child, isTerminal, env, result, depth);
	    }
	}




	/**
	 * If the node'size is above a certain ration of the size of the bounds,
	 * it is placed into result. Otherwise, it is recursively split until
	 * the child nodes' ratio to given bounds has become large enough.
	 *
	 * Use example:
	 * If the screen is centered on a certain location, then this method
	 * picks tiles (quad-tree-nodes) of appropriate size (not too big and not too small).
	 *
	 *
	 * @param bounds
	 * @param depth
	 * @param result
	 */
	public static <N> void splitFor(QuadTreeNodeOps<N> ops, N node, Predicate<? super N> isTerminalNode, List<N> result, Envelope env, int depth) {
	    /*
	    console.log("Depth = " + depth);
	    console.log(this.getBounds());
	    */
	
	
	    /*
	    if(depth > 10) {
	        result.push(this);
	        return;
	    }*/
	
		//if()
		
		Envelope nodeEnv = ops.getEnvelope(node);
		
	    if(!nodeEnv.intersects(env)) {
	        return;
	    }
	
	    // If the node is loaded, avoid splitting it
	    
	    boolean loaded = isTerminalNode.test(node);
	    if(loaded) {
	    	result.add(node);
	    	return;
	    }
	
	    // How many times the current node is bigger than the view rect
	    double w = env.getWidth() / nodeEnv.getWidth();
	    double h = env.getHeight() / nodeEnv.getHeight();
	
	    double r = Math.max(w, h);
	    //var r = Math.min(w, h);
	
	    int nodeDepth = ops.getDepth(node);
	    int nodeMaxDepth = ops.getMaxDepth(node);
	    
	    if(r >= depth || nodeDepth >= nodeMaxDepth) {
	        result.add(node);
	    }
	
	    if(!ops.hasChildren(node)) {
	        ops.subdivide(node);
	    }
	
	    List<N> children = ops.getChildren(node);
	    for(N child : children) {
	    	splitFor(ops, child, isTerminalNode, result, env, depth);
	    }
	}


	public static <N> List<N> aquireNodes(QuadTreeNodeOps<N> ops, N node, Predicate<? super N> isTerminalNode, Envelope env, int depth) {
	    List<N> result = new ArrayList<>();
	
	    splitFor(ops, node, isTerminalNode, result, env, depth);
	
	    return result;
	}


//unlink: function() {
//    if(!this.parent) {
//        return;
//    }
//
//    for(var i in this.parent.children) {
//        var child = this.parent.children[i];
//
//        if(child == this) {
//            this.parent.children = new QuadTreeNode(this.parent, this._bounds, this._depth, this._k);
//        }
//    }
//}
}
