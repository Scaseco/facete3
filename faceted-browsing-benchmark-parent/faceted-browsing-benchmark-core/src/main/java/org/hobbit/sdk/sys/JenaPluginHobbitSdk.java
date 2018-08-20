package org.hobbit.sdk.sys;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginHobbitSdk
	implements JenaSubsystemLifecycle
{

	public void start() {
		JenaPluginHobbitSdkUtils.init();
	}

	@Override
	public void stop() {
	}
}
