package org.aksw.jena_sparql_api.changeset.ex.api;

import java.util.Set;

import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.apache.jena.rdf.model.Resource;

public interface ChangeSetGroup
	extends Resource
{
	void setPrecedingChangeSetGroup(ChangeSetGroup precedingChangeSetGroup);
	ChangeSetGroup getPrecedingChangeSetGroup();
	Set<ChangeSet> members();
}
