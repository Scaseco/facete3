package org.aksw.facete.v3.bgp.api;

import org.aksw.facete.v3.api.DirNodeNavigation;

/**
 * 
 * @author raven
 *
 */
public interface BgpDirNode extends DirNodeNavigation<BgpMultiNode> {
	boolean isFwd();
}
