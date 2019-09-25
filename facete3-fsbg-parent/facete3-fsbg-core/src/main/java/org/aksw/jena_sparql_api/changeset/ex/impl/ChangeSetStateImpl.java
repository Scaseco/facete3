package org.aksw.jena_sparql_api.changeset.ex.impl;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.ex.api.CSX;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class ChangeSetStateImpl
	extends ResourceImpl
	implements ChangeSetState
{
	public ChangeSetStateImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public ChangeSet getLatestChangeSet() {
		return ResourceUtils.getPropertyValue(this, CSX.latestChangeSet, ChangeSet.class);
	}

	@Override
	public void setLatestChangeSet(ChangeSet changeSet) {
		ResourceUtils.setProperty(this, CSX.latestChangeSet, changeSet);		
	}

	@Override
	public boolean isUndone() {
		return ResourceUtils.tryGetLiteralPropertyValue(this, CSX.isLatestChangeSetUndone, Boolean.class).orElse(false);
	}

	@Override
	public void setUndone(boolean onOrOff) {
		ResourceUtils.setLiteralProperty(this, CSX.isLatestChangeSetUndone, onOrOff == false ? null : true);
	}
}
