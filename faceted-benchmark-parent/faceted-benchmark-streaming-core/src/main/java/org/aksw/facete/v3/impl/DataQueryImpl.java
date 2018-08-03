package org.aksw.facete.v3.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.facete.v3.api.DataMultiNode;
import org.aksw.facete.v3.api.DataNode;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggSample;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.hobbit.benchmark.faceted_browsing.v2.main.QueryGroupExecutor;

import com.google.common.collect.Iterators;

import io.reactivex.Flowable;

public class DataQueryImpl<T extends RDFNode>
	implements DataQuery<T>
{
	protected SparqlQueryConnection conn;
	
	protected Node rootVar;
	protected Element baseQueryPattern;
	
	protected Template template;
	
	protected List<DataNode> dataNodes;
	
//	protected Range<Long> range;

	protected Long limit;
	protected Long offset;
	
	protected boolean sample;
	protected Class<T> resultClass;

	public DataQueryImpl(SparqlQueryConnection conn, Node rootNode, Element baseQueryPattern, Template template, Class<T> resultClass) {
		super();
		this.conn = conn;
		this.rootVar = rootNode;
		this.baseQueryPattern = baseQueryPattern;
		this.template = template;
		this.resultClass = resultClass;
	}

	public <U extends RDFNode> DataQuery<U> as(Class<U> clazz) {
		return new DataQueryImpl<U>(conn, rootVar, baseQueryPattern, template, clazz);
	}
	
	@Override
	public DataQuery<T> limit(Long limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public DataQuery<T> offset(Long offset) {
		this.offset = offset;
		return this;
	}
	
	@Override
	public DataQuery<T> sample(boolean onOrOff) {
		this.sample = onOrOff;
		return this;
	}
	
	@Override
	public boolean sample() {
		return sample;
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
	public Flowable<T> exec() {
		
		Set<Var> vars = new LinkedHashSet<>();
		if(rootVar.isVariable()) {
			vars.add((Var)rootVar);
		}
		
		if(template != null) {
			vars.addAll(PatternVars.vars(new ElementTriplesBlock(template.getBGP())));
		}

		Query query = new Query();
		//query.setQueryResultStar(true);
		query.setQuerySelectType();

		for(Var v : vars) {
			query.getProject().add(v);
		}
		

		if(sample) {
			Set<Var> allVars = new LinkedHashSet<>();
			allVars.addAll(vars);
			allVars.addAll(PatternVars.vars(baseQueryPattern));
			
			Generator<Var> varGen = VarGeneratorBlacklist.create(allVars);
			Var innerRootVar = varGen.next();
			
//			if(baseQueryPattern instanceof ElementSubQuery) {
//				QueryGroupExecutor.createQueryGroup()
//
//			}
			
			Element innerE = ElementUtils.createRenamedElement(baseQueryPattern, Collections.singletonMap(rootVar, innerRootVar));

			
			Query inner = new Query();
			inner.setQuerySelectType();
			inner.setQueryPattern(innerE);
			Expr agg = inner.allocAggregate(new AggSample(new ExprVar(innerRootVar)));
			inner.getProject().add((Var)rootVar, agg);
			QueryUtils.applySlice(inner, offset, limit, false);

			Element e = ElementUtils.groupIfNeeded(new ElementSubQuery(inner), baseQueryPattern);
						
			query.setQueryPattern(e);
		} else {
			
			// TODO Controlling distinct should be possible on this class
			query.setDistinct(true);
	
			query.setQueryPattern(baseQueryPattern);
			QueryUtils.applySlice(query, offset, limit, false);
		}
		
		//System.out.println(query);

		
		Flowable<T> result = ReactiveSparqlUtils
			// FIXME WHY DO WE GET AN EMPTY RESULT SET WHEN WE USE THE QUERY OBJECT???
			.execSelect(() -> conn.query("" + query))
			.map(b -> {
				Graph graph = GraphFactory.createDefaultGraph();

				// TODO Re-allocate blank nodes
				if(template != null) {
					Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Iterators.singletonIterator(b));
					while(it.hasNext()) {
						Triple t = it.next();
						graph.add(t);
					}
				}

				Node rootNode = rootVar.isVariable() ? b.get((Var)rootVar) : rootVar;
				
				Model m = ModelFactory.createModelForGraph(graph);
				RDFNode r = m.asRDFNode(rootNode);
				
//				Resource r = m.createResource()
//				.addProperty(RDF.predicate, m.asRDFNode(valueNode))
//				.addProperty(Vocab.facetValueCount, );
//			//m.wrapAsResource(valueNode);
//			return r;

				return r;
			})
			.map(r -> r.as(resultClass));
		
		return result;
	}
	
	
}
