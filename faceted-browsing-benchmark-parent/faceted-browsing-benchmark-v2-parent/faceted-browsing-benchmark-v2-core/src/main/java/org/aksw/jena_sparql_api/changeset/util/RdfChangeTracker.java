package org.aksw.jena_sparql_api.changeset.util;

import org.apache.jena.rdf.model.Model;

public interface RdfChangeTracker
	extends ChangeTracker<Model>
{
	Model getDataModel();
	Model getChangeModel();
}
