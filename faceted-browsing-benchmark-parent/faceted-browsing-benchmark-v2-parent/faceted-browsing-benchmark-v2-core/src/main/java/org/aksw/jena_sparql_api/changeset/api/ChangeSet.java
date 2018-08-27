package org.aksw.jena_sparql_api.changeset.api;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface ChangeSet
	extends Resource
{
	ChangeSet getPrecedingChangeSet();
	void setPrecedingChangeSet(Resource resource);
	
	RDFNode getChangeReason();
	void setChangeReason(RDFNode changeReason);

	default void setChangeReason(String changeReason) {
		RDFNode literal = this.getModel().createLiteral(changeReason);
		setCreatorName(literal);
	}

	
	RDFNode getCreatorName();
	void setCreatorName(RDFNode creatorName);

	default void setCreatorName(String creatorName) {
		RDFNode literal = this.getModel().createLiteral(creatorName);
		setCreatorName(literal);
	}
	

	Resource getSubjectOfChange();
	void setSubjectOfChange(Resource resource);
	
	Collection<RdfStatement> additions();
	Collection<RdfStatement> removals();
}
