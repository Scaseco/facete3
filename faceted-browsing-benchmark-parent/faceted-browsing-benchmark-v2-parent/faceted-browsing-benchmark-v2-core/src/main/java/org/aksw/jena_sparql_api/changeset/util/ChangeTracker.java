package org.aksw.jena_sparql_api.changeset.util;

import java.util.function.Consumer;

public interface ChangeTracker<T>
	extends ChangeApi
{
	void trackChanges(Consumer<T> action);
}
