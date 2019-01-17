package org.aksw.jena_sparql_api.changeset.util;

import org.apache.jena.rdf.model.Model;

/**
 * The name ChangeSetGroupManager suggests a lower level API,
 * whereas this class should be used in client code. 
 * 
 * @author raven
 *
 */
public class RdfChangeSetTrackerImpl
	extends ChangeSetGroupManager
{
	public RdfChangeSetTrackerImpl(Model model) {
		this(model, model);
	}

	public RdfChangeSetTrackerImpl(Model changeModel, Model dataModel) {
		super(changeModel, dataModel);
	}
}
