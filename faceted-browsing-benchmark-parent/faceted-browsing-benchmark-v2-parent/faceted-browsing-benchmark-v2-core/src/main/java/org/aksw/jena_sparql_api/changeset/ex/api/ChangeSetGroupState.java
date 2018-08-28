package org.aksw.jena_sparql_api.changeset.ex.api;

import org.apache.jena.rdf.model.Resource;

public interface ChangeSetGroupState
	extends Resource
{
	ChangeSetGroup getLatestChangeSetGroup();
	void setLatestChangeSetGroup(ChangeSetGroup changeSetGroup);
	
	boolean isUndone();
	void setUndone(boolean onOrOff);
}
