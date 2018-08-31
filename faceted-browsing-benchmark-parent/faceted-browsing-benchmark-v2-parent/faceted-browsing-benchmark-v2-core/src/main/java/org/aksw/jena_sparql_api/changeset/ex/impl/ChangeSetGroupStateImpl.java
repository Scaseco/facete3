package org.aksw.jena_sparql_api.changeset.ex.impl;

import java.util.Map;

import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.changeset.ex.api.CSX;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroup;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetGroupState;
import org.aksw.jena_sparql_api.changeset.ex.api.ChangeSetState;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class ChangeSetGroupStateImpl
	extends ResourceBase
	implements ChangeSetGroupState
{
	public ChangeSetGroupStateImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public ChangeSetGroup getLatestChangeSetGroup() {
		return ResourceUtils.getPropertyValue(this, CSX.latestChangeSetGroup, ChangeSetGroup.class);
	}

	@Override
	public void setLatestChangeSetGroup(ChangeSetGroup changeSetGroup) {
		ResourceUtils.setProperty(this, CSX.latestChangeSetGroup, changeSetGroup);		
	}

	@Override
	public boolean isUndone() {
		return ResourceUtils.tryGetLiteralPropertyValue(this, CSX.isLatestChangeSetGroupUndone, Boolean.class).orElse(false);
	}

	@Override
	public void setUndone(boolean onOrOff) {
		ResourceUtils.setLiteralProperty(this, CSX.isLatestChangeSetGroupUndone, onOrOff == false ? null : true);
	}

	@Override
	public Map<Resource, ChangeSetState> resourceStates() {
		//return new MapFromProperty(this, , CSX.resource)
		return null;
	}
}
