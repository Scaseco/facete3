package org.aksw.jena_sparql_api.data_query.impl;

import org.aksw.jena_sparql_api.data_query.api.Selection;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDFS;

public class SelectionImpl
	extends ResourceImpl
	implements Selection
{
    public SelectionImpl(Node n, EnhGraph m) {
        super(n, m);
    }

	@Override
	public void setAlias(Var alias) {
		// TODO use proper vocab
		ResourceUtils.setLiteralProperty(this, RDFS.seeAlso, alias.getName());
	}

	@Override
	public Var getAlias() {
		Var result = ResourceUtils.getLiteralProperty(this, RDFS.seeAlso, String.class)
		//Var result = Optional.ofNullable(getProperty(RDFS.seeAlso))
			.map(Statement::getString)
			.map(Var::alloc)
			.orElse(null);
		
		return result;
	}
}
