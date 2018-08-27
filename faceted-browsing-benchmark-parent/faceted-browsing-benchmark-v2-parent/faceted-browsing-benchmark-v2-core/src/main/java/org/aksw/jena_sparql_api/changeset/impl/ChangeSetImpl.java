package org.aksw.jena_sparql_api.changeset.impl;

import java.util.Collection;

import org.aksw.facete.v3.impl.ResourceBase;
import org.aksw.jena_sparql_api.changeset.CS;
import org.aksw.jena_sparql_api.changeset.api.ChangeSet;
import org.aksw.jena_sparql_api.changeset.api.RdfStatement;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ChangeSetImpl
	extends ResourceBase
	implements ChangeSet
{
	public ChangeSetImpl(Node n, EnhGraph m) {
		super(n, m);
	}

	@Override
	public ChangeSet getPrecedingChangeSet() {
		return ResourceUtils.getPropertyValue(this, CS.precedingChangeSet, ChangeSet.class);
	}

	@Override
	public void setPrecedingChangeSet(Resource resource) {
		ResourceUtils.setProperty(this, CS.precedingChangeSet, resource);	
	}

	@Override
	public RDFNode getChangeReason() {
		return ResourceUtils.getPropertyValue(this, CS.changeReason, ChangeSet.class);
	}

	@Override
	public void setChangeReason(RDFNode changeReason) {
		ResourceUtils.setProperty(this, CS.changeReason, changeReason);
	}

	@Override
	public RDFNode getCreatorName() {
		return ResourceUtils.getPropertyValue(this, CS.creatorName);	
	}

	@Override
	public void setCreatorName(RDFNode creatorName) {
		ResourceUtils.setProperty(this, CS.creatorName, creatorName);	
	}

	@Override
	public Resource getSubjectOfChange() {
		return ResourceUtils.getPropertyValue(this, CS.subjectOfChange, Resource.class);
	}

	@Override
	public void setSubjectOfChange(Resource subjectOfChange) {
		ResourceUtils.setProperty(this, CS.subjectOfChange, subjectOfChange);
	}

	@Override
	public Collection<RdfStatement> additions() {
		Collection<RdfStatement> result = new SetFromPropertyValues<>(this, CS.addition, RdfStatement.class);
		return result;
	}

	@Override
	public Collection<RdfStatement> removals() {
		Collection<RdfStatement> result = new SetFromPropertyValues<>(this, CS.removal, RdfStatement.class);
		return result;
	}	
}
