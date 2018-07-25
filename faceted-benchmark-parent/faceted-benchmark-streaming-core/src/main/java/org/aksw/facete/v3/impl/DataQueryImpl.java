package org.aksw.facete.v3.impl;

import java.util.List;

import org.aksw.facete.v3.api.DataNode;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.syntax.Element;

import com.google.common.collect.Range;

public class DataQueryImpl
	implements DataQuery
{
	protected RDFConnection conn;
	protected Element baseQueryPattern;
		
	protected List<DataNode> dataNodes;
	
	protected Range<Long> range;
	
	
	public void setLimit() {
		
	}
	//protected void setOffset(10);
	
	@Override
	public Concept getPredicates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultiNode add(Property property) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
