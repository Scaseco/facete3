package org.aksw.jena_sparql_api.changeset.util;

public interface RdfChangeTrackerWrapper
	extends RdfChangeTracker
{
	// The public view which should be written to by client code
	
	// TODO Move this method to a better place - its not RDF specific
	/**
	 * Discard uncommited changes
	 * 
	 */
	void discardChanges();
	
	/**
	 * Write pending chagnes to the underlying storage including
	 * tracking information for future undo.
	 * 
	 */
	void commitChanges();
	
	/**
	 * Write any pending changes to the underlying storage, but without
	 * creating a commit record for it, effectively excluding these changes
	 * from future undo calls.
	 * 
	 * Care must be taken to not make undo information inconsistent.
	 * 
	 */
	void commitChangesWithoutTracking();
}
