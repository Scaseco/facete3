package org.aksw.jena_sparql_api.changeset.impl;

import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.changeset.api.RdfStatement;
import org.aksw.jena_sparql_api.utils.model.NodeMapperFactory;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

public class RdfStatementImpl
	extends ResourceBase
	implements RdfStatement
{

	public RdfStatementImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public Resource getSubject() {
		return ResourceUtils.getPropertyValue(this, RDF.subject, Resource.class);
	}

	@Override
	public void setSubject(Resource subject) {
		ResourceUtils.setProperty(this, RDF.subject, subject);
	}

	@Override
	public Property getPredicate() {
		Property result = ResourceUtils.tryGetPropertyValue(this, RDF.predicate, NodeMapperFactory.uriString)
			.map(r -> ResourceFactory.createProperty(r))
			.orElse(null);
		
		return result;
	}

	@Override
	public void setPredicate(Property predicate) {
		ResourceUtils.setProperty(this, RDF.predicate, predicate);
	}

	@Override
	public RDFNode getObject() {
		return ResourceUtils.getPropertyValue(this, RDF.object);
	}

	@Override
	public void setObject(RDFNode object) {
		ResourceUtils.setProperty(this, RDF.object, object);
	}

}
