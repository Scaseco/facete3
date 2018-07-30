package org.aksw.facete.v3.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.facete.v3.api.DataMultiNode;
import org.aksw.facete.v3.api.DataNode;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

import com.google.common.collect.Iterators;

import io.reactivex.Flowable;

public class DataQueryImpl
	implements DataQuery
{
	protected SparqlQueryConnection conn;
	
	protected Var rootVar;
	protected Element baseQueryPattern;
	
	protected Template template;
	
	protected List<DataNode> dataNodes;
	
//	protected Range<Long> range;

	protected Long limit;
	protected Long offset;

	public DataQueryImpl(SparqlQueryConnection conn, Var rootVar, Element baseQueryPattern, Template template) {
		super();
		this.conn = conn;
		this.rootVar = rootVar;
		this.baseQueryPattern = baseQueryPattern;
		this.template = template;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
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
		Query query = new Query();

		query.setQueryPattern(baseQueryPattern);
		QueryUtils.applySlice(query, offset, limit, false);
		
		Flowable<Resource> result = ReactiveSparqlUtils
			.execSelect(() -> conn.query(query))
			.map(b -> {
				Graph graph = GraphFactory.createDefaultGraph();

				Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Iterators.singletonIterator(b));
				while(it.hasNext()) {
					Triple t = it.next();
					graph.add(t);
				}
				
				Node rootNode = b.get(rootVar);
				Model m = ModelFactory.createModelForGraph(graph);
				Resource r = m.wrapAsResource(rootNode);
				return r;
			});
		
		return result;
	}
	
	
}
