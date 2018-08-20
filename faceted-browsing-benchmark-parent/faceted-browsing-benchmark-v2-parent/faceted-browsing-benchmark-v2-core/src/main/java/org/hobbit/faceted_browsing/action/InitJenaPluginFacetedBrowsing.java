package org.hobbit.faceted_browsing.action;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitJenaPluginFacetedBrowsing
	implements JenaSubsystemLifecycle
{
	public void start() {
		JenaPluginFacetedBrowsing.init();
	}

	@Override
	public void stop() {
	}
}
