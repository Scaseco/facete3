package org.aksw.facete.v3.impl;

import java.util.List;
import java.util.Map.Entry;

import org.aksw.facete.v3.api.DataMultiNode;
import org.aksw.facete.v3.api.DataNode;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

import com.google.common.collect.Range;

import io.reactivex.Flowable;

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
	public Concept fetchPredicates() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public DataNode getRoot() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public DataMultiNode add(Property property) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public DataQuery filter(UnaryRelation concept) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Entry<Var, Query> toConstructQuery() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Flowable<Resource> exec() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
