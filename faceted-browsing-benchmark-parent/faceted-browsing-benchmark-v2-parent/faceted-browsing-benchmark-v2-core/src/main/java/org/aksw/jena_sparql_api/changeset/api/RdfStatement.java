package org.aksw.jena_sparql_api.changeset.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface RdfStatement
	extends Resource
{
	Resource getSubject();
	Property getPredicate();
	RDFNode getObject();

	void setSubject(Resource subject);
	void setPredicate(Property predicate);
	void setObject(RDFNode object);
}
