package org.aksw.jena_sparql_api.changeset.ex.api;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.apache.jena.rdf.model.Resource;

public interface ChangeSetState
	extends Resource
{
	ChangeSet getLatestChangeSet();
	void setLatestChangeSet(ChangeSet changeSet);
	
	boolean isUndone();
	void setUndone(boolean onOrOff);
}
