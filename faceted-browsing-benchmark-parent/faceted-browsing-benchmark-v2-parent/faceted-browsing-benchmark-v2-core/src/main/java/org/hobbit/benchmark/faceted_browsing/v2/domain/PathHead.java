package org.hobbit.benchmark.faceted_browsing.v2.domain;

import org.apache.jena.sparql.path.Path;

/**
 * PathHead combines a Path with a flag indicating
 * a direction (facing forward or backward).
 * 
 * @author Claus Stadler, May 24, 2018
 *
 */
public class PathHead {
	protected Path path;
	protected boolean isReverse;

	public PathHead(Path path, boolean isReverse) {
		super();
		this.path = path;
		this.isReverse = isReverse;
	}
}
