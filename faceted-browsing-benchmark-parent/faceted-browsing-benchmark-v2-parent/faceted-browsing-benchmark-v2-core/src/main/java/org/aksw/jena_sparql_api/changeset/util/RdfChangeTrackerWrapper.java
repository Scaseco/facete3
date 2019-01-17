package org.aksw.jena_sparql_api.changeset.util;

public interface RdfChangeTrackerWrapper
	extends RdfChangeTracker
{
	// The public view which should be written to by client code
	
	// TODO Move this method to a better place - its not RDF specific
	void discardChanges();
	void commitChanges();
	
	void commitChangesWithoutTracking();
}
