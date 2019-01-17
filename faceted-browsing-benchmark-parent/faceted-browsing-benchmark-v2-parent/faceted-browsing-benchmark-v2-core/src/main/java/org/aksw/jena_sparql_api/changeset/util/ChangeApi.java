package org.aksw.jena_sparql_api.changeset.util;

public interface ChangeApi {
	void undo();
	void redo();

	boolean canUndo();
	boolean canRedo();

	void clearRedo();
}
