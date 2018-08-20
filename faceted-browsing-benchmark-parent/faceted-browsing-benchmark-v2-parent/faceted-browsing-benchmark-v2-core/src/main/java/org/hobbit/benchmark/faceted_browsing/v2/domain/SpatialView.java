package org.hobbit.benchmark.faceted_browsing.v2.domain;

import java.awt.Rectangle;

/**
 * A view over a concept for interacting with the spatial dimension
 * of a concept.
 * 
 * TODO Rectangle is not really the appropriate class, as it is tied to the window toolkit
 * 
 * @author Claus Stadler, May 24, 2018
 *
 */
public interface SpatialView {
	void setBoundingBox(Rectangle bbox);
	Rectangle getBoundingBox();
}
