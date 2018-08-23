package org.hobbit.qpid.v7.plugin.hobbit;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class HobbitPluginQpid7
implements JenaSubsystemLifecycle {

public void start() {
	HobbitPluginQpid7Utils.init();
}

@Override
public void stop() {
}
}
