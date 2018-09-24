package org.aksw.facete.v3.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.aksw.facete.v3.api.DataMultiNode;
import org.aksw.facete.v3.api.DataNode;
import org.aksw.facete.v3.api.DataQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
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
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Random;
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

import com.google.common.collect.Iterators;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class DataQueryImpl<T extends RDFNode>
	implements DataQuery<T>
{
	protected SparqlQueryConnection conn;
	
//	protected Node rootVar;
//	protected Element baseQueryPattern;

	
	// FIXME for generalization, probably this attribute has to be replaced by
	// a something similar to a list of roots; ege DataNode
	protected Relation baseRelation;
	
	protected Template template;
	
	protected List<DataNode> dataNodes;
	
//	protected Range<Long> range;

	protected Long limit;
	protected Long offset;
	
	protected UnaryRelation filter;
	
	protected boolean randomOrder;
	protected boolean sample;
	protected Class<T> resultClass;

	protected List<SortCondition> sortConditions;
	
	public DataQueryImpl(SparqlQueryConnection conn, Node rootNode, Element baseQueryPattern, Template template, Class<T> resultClass) {
		this(conn, new Concept(baseQueryPattern, (Var)rootNode), template, resultClass);
	}
		
	public DataQueryImpl(SparqlQueryConnection conn, Relation baseRelation, Template template, Class<T> resultClass) {
		super();
		this.conn = conn;
//		this.rootVar = rootNode;
//		this.baseQueryPattern = baseQueryPattern;
		this.baseRelation = baseRelation;
		this.template = template;
		this.resultClass = resultClass;
	}

	@Override
	public SparqlQueryConnection connection() {
		return conn;
	}
	
	@Override
	public DataQuery<T> connection(SparqlQueryConnection connection) {
		this.conn = connection;
		return this;
	}
	
	public <U extends RDFNode> DataQuery<U> as(Class<U> clazz) {
		return new DataQueryImpl<U>(conn, baseRelation, template, clazz);
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
	public boolean isSampled() {
		return sample;
	}
	
	@Override
	public boolean isRandomOrder() {
		return randomOrder;
	}

	@Override
	public DataQuery<T> randomOrder(boolean onOrOff) {
		this.randomOrder = onOrOff;
		return this;
//		return this;
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
	public DataQuery<T> filter(UnaryRelation concept) {
		if(filter == null) {
			filter = concept;
		} else {
			filter = filter.joinOn(filter.getVar()).with(concept).toUnaryRelation();
		}
		
		return this;
	}


	@Override
	public Entry<Var, Query> toConstructQuery() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Flowable<T> exec() {
		Objects.requireNonNull(conn);
		
		
		Set<Var> vars = new LinkedHashSet<>();
		Node rootVar = baseRelation.getVars().get(0);
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
		
		Element baseQueryPattern = baseRelation.getElement();
		
		Element effectivePattern = filter == null
				? baseQueryPattern
				: new RelationImpl(baseQueryPattern, new ArrayList<>(PatternVars.vars(baseQueryPattern))).joinOn((Var)rootVar).with(filter).getElement()
				;

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
			
			Element innerE = ElementUtils.createRenamedElement(effectivePattern, Collections.singletonMap(rootVar, innerRootVar));

			
			Query inner = new Query();
			inner.setQuerySelectType();
			inner.setQueryPattern(innerE);
			Expr agg = inner.allocAggregate(new AggSample(new ExprVar(innerRootVar)));
			inner.getProject().add((Var)rootVar, agg);
			QueryUtils.applySlice(inner, offset, limit, false);

			Element e = ElementUtils.groupIfNeeded(new ElementSubQuery(inner), effectivePattern);
						
			query.setQueryPattern(e);
		} else {
			
			// TODO Controlling distinct should be possible on this class
			query.setDistinct(true);
	
			query.setQueryPattern(effectivePattern);
			QueryUtils.applySlice(query, offset, limit, false);
		}
		
		if(randomOrder) {
			query.addOrderBy(new E_Random(), Query.ORDER_ASCENDING);
		}
		
		System.out.println("Generated query: " + query);

		
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

	@Override
	public Relation baseRelation() {
//		Element effectivePattern = filter == null
//				? baseQueryPattern
//				: new RelationImpl(baseQueryPattern, new ArrayList<>(PatternVars.vars(baseQueryPattern))).joinOn((Var)rootVar).with(filter).getElement()
//				;

		//UnaryRelation result = new Concept(baseQueryPattern, (Var)rootVar);
		return baseRelation;
	}

	@Override
	public Single<Model> execConstruct() {
		return exec().toList().map(l -> {
			Model r = ModelFactory.createDefaultModel();
			for(RDFNode item : l) {
				Model tmp = item.getModel();
				if(tmp != null) {
					r.add(tmp);
				}
			}
			return r;
		});
	}	
	
}
