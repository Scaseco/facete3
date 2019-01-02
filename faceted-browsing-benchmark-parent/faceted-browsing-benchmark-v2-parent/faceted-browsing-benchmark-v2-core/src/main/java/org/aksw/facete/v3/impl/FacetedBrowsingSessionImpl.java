package org.aksw.facete.v3.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.hobbit.benchmark.faceted_browsing.v2.domain.FactoryWithModel;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessorSPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPath;
import org.hobbit.benchmark.faceted_browsing.v2.main.FacetedQueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

import io.reactivex.Flowable;

public class FacetedBrowsingSessionImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(FacetedBrowsingSessionImpl.class);

	
	protected RDFConnection conn;
	protected SPath root;
	
	protected SPath focus;

	protected FacetedQueryGenerator<SPath> queryGenerator;
	
	public FacetedBrowsingSessionImpl(RDFConnection conn) {
		super();
		this.conn = conn;
		
		FactoryWithModel<SPath> pathFactory = new FactoryWithModel<>(SPath.class);
		
		root = pathFactory.get();		

		PathAccessor<SPath> pathAccessor = new PathAccessorSPath();
		queryGenerator = new FacetedQueryGenerator<>(pathAccessor);
		
		this.focus = root;
		
//		queryGenerator.createMapFacetsAndValues(root, false, false);
	}
	
	public SPath getRoot() {
		return root;
	}

	/**
	 * Returns a flow of mappings from predicate to count.
	 * If the count is known, the range will include a single element,
	 * otherwise it may be created with .atLeast() or .atMost();
	 * 
	 * @param path
	 * @param isReverse
	 * @return
	 */
//	public Flowable<Entry<Node, Range<Long>>> getFacetsAndCounts(SPath path, boolean isReverse, Concept pConstraint) {
//		BinaryRelation br = createQueryFacetsAndCounts(path, isReverse, pConstraint);
//		
//		
//		//RelationUtils.attr
//		
//		Query query = RelationUtils.createQuery(br);
//		
//		logger.info("Requesting facet counts: " + query);
//		
//		return ReactiveSparqlUtils.execSelect(() -> conn.query(query))
//			.map(b -> new SimpleEntry<>(b.get(br.getSourceVar()), Range.singleton(((Number)b.get(br.getTargetVar()).getLiteral().getValue()).longValue())));
//	}
	
	public BinaryRelation createQueryFacetsAndCounts(SPath path, boolean isReverse, Concept pConstraint) {
		Map<String, BinaryRelation> relations = null;
		//		Map<String, BinaryRelation> relations = queryGenerator.createMapFacetsAndValues(path, isReverse, false);

		// Align the relations
		//Relation aligned = FacetedBrowsingSession.align(relations.values(), Arrays.asList(Vars.p, Vars.o));
		
		
//		List<Relation> aligned = relations.values().stream()
//				.map(r -> FacetedBrowsingSession.align(r, Arrays.asList(Vars.p, Vars.o)))
//				.collect(Collectors.toList());
		
		
//		Var countVar = Var.alloc("__count__");
//		List<Element> elements = relations.values().stream()
//				.map(e -> rename(e, Arrays.asList(Vars.p, Vars.o)))
//				.map(Relation::toBinaryRelation)
//				.map(e -> e.joinOn(e.getSourceVar()).with(pConstraint))
//				.map(e -> groupBy(e, Vars.o, countVar))
//				.map(Relation::getElement)
//				.collect(Collectors.toList());
//		
//		Element e = ElementUtils.union(elements);
//
//		BinaryRelation result = new BinaryRelationImpl(e, Vars.p, countVar);

		BinaryRelation result = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint, false);
		
		return result;
		//Map<String, TernaryRelation> facetValues = g.getFacetValues(focus, path, false);
	}

	public static Relation rename(Relation r, List<Var> targetVars) {
		
		
		Set<Var> relationVars = new LinkedHashSet<>(r.getVars());
		Set<Var> vs = new LinkedHashSet<>(targetVars);
		if(vs.size() != relationVars.size()) {
			throw new IllegalArgumentException("Number of distinct variables of the relation must match the number of distinct target variables");
		}
		
		Map<Var, Var> rename = Streams.zip(
			relationVars.stream(),
			vs.stream(),
			(a, b) -> new SimpleEntry<>(a, b))
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		// Extend the map by renaming all remaining variables
		Set<Var> mentionedVars = r.getVarsMentioned();
		Set<Var> remainingVars = Sets.difference(mentionedVars, relationVars);
		
		Map<Var, Var> map = VarUtils.createDistinctVarMap(targetVars, remainingVars, false, VarGeneratorImpl2.create());
		map.putAll(rename);
		
		Relation result = r.applyNodeTransform(new NodeTransformSubst(map));
		
//		System.out.println("RENAMED");
//		System.out.println(r);
//		System.out.println("TO");
//		System.out.println(result);
		
		return result;
	}
	
	/**
	 * Rename variables of all relations to the given list of variables
	 * All relations and the list of given variables must have the same length
	 * 
	 * @param relations
	 * @return
	 */
	public static Relation align(Collection<? extends Relation> relations, List<Var> vars) {
		List<Relation> tmp = relations.stream()
				.map(r -> FacetedBrowsingSessionImpl.rename(r, vars))
				.collect(Collectors.toList());

		List<Element> es = tmp.stream()
				.map(Relation::getElement)
				.collect(Collectors.toList());

		
		Element e = ElementUtils.unionIfNeeded(es);
		Relation result = new RelationImpl(e, vars);
		return result;
	}

	
	public static Relation groupBy(Relation r, Var aggVar, Var resultVar, boolean includeAbsent) {
		Query query = new Query();
		query.setQuerySelectType();
		query.setQueryPattern(r.getElement());
		
		ExprVar ev = new ExprVar(aggVar);
		
		Expr e = includeAbsent
				? new E_Conditional(new E_Bound(ev), ev, ConstraintFacadeImpl.NV_ABSENT)
				: ev;
		Expr tmp = query.allocAggregate(new AggCountVarDistinct(e));
		
		List<Var> vars = r.getVars();

		// Add all other vars as group vars
		List<Var> groupVars = vars.stream()
				.filter(v -> !aggVar.equals(v))
				.collect(Collectors.toList());
	
		query.addProjectVars(groupVars);
		query.getProject().add(resultVar, tmp);
		
		List<Var> newVars = new ArrayList<>(groupVars);
		newVars.add(resultVar);
		
		for(Var groupVar : groupVars) {
			query.addGroupBy(groupVar);
		}
		
		Relation result = new RelationImpl(new ElementSubQuery(query), newVars);
		return result;
	}
	
	
	public Flowable<Cell<Node, Node, Range<Long>>> getFacetValues(SPath facetPath, boolean isReverse, Concept pFilter, Concept oFilter) {
		TernaryRelation tr = createQueryFacetValues(facetPath, isReverse, pFilter, oFilter);
		Query query = tr.toQuery();
//		Query query = RelationUtils.createQuery(tr);
		
		logger.info("Requesting facet value counts: " + query);
		
		
		return ReactiveSparqlUtils.execSelect(() -> conn.query(query))
			.map(b -> Tables.immutableCell(b.get(tr.getS()), b.get(tr.getP()), Range.singleton(((Number)b.get(tr.getO()).getLiteral().getValue()).longValue())));

	}
	
	public TernaryRelation createQueryFacetValues(SPath facetPath, boolean isReverse, Concept pFilter, Concept oFilter) {
	
		Map<String, TernaryRelation> facetValues = queryGenerator.getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse, false, false, false);

		Var countVar = Vars.c;
		List<Element> elements = facetValues.values().stream()
				.map(e -> rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
				.map(Relation::toTernaryRelation)
				.map(e -> e.joinOn(e.getP()).with(pFilter))
				.map(e -> groupBy(e, Vars.s, countVar, false))
				.map(Relation::getElement)
				.collect(Collectors.toList());

		
		Element e = ElementUtils.unionIfNeeded(elements);

		TernaryRelation result = new TernaryRelationImpl(e, Vars.p, Vars.o, countVar);

		
		
		return result;

//		FacetedBrowsingSession.align(r, Arrays.asList(Vars.s, Vars.p, Vars.o)
//		List<Relation> aligned = facetValues.values().stream()
//		.map(r -> ))
//		.collect(Collectors.toList());

		
		
//		Map<String, TernaryRelation> map = facetValues.entrySet().stream()
//		.collect(Collectors.toMap(Entry::getKey, e -> FacetedQueryGenerator.countFacetValues(e.getValue(), -1)));
		
	}
	
	/**
	 * TODO How to do combined filters such as "label must match a regex pattern and the count < 1000"? 
	 * 
	 * @param filter
	 */
//	public getFacetValuesAndCounts(Concept filter) {
//		Map<String, TernaryRelation> map = facetValues.entrySet().stream()
//		.collect(Collectors.toMap(Entry::getKey, e -> FacetedQueryGenerator.countFacetValues(e.getValue(), -1)));
//
//	}
//	
//	
//	// TODO Move to concept utils
//	public static valuesToFilter() {
//		
//	}
}
