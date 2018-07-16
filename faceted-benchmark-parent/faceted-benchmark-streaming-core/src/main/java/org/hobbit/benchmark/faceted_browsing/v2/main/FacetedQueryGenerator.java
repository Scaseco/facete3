package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.hobbit.benchmark.faceted_browsing.v2.domain.ExprTransformViaPathMapper;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.QueryFragment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import jersey.repackaged.com.google.common.collect.Sets;

public class FacetedQueryGenerator<P> {
	protected Concept baseConcept;
	protected PathToRelationMapper<P> mapper;
	protected Collection<Expr> constraints;
	
	//protected Class<P> pathClass;
	protected PathAccessor<P> pathAccessor;
	
	
	
	public FacetedQueryGenerator(PathAccessor<P> pathAccessor) {
		super();
		this.pathAccessor = pathAccessor;
		this.baseConcept = ConceptUtils.createSubjectConcept();
		this.mapper = new PathToRelationMapper<>(pathAccessor);
		this.constraints = new LinkedHashSet<>();
	}

	public Collection<Expr> getConstraints() {
		return constraints;
	}

	
	public BinaryRelation getFacets(P childPath, SetMultimap<P, Expr> childPathToExprs, Set<Expr> constraints) {
		Set<Expr> excludes = childPathToExprs.get(childPath);

		Set<Expr> effectiveConstraints = Sets.difference(constraints, excludes);
		
		BinaryRelation rel = pathAccessor.getReachingRelation(childPath);
		
		// TODO If the relation is of form ?s <p> ?o, then rewrite as ?s ?p ?o . FILTER(?p = <p>)
		
		List<Element> elts = ElementUtils.toElementList(rel.getElement());
		elts.add(new ElementBind(Vars.p, NodeValue.makeNode(NodeFactory.createURI(pathAccessor.getPredicate(childPath)))));
		
		rel = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), rel.getSourceVar(), rel.getTargetVar());
		
		BinaryRelation result = getFacets(pathAccessor.getParent(childPath), rel, Vars.p, effectiveConstraints);
		return result;
	}

	public BinaryRelation getRemainingFacets(P basePath, boolean isReverse, Set<Expr> effectiveConstraints) {
		BinaryRelation br = new BinaryRelationImpl(
				ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, Vars.p, Vars.o)), Vars.s, Vars.o);
		
		BinaryRelation result = getFacets(basePath, br, Vars.p, effectiveConstraints);

		return result;
	}

	public BinaryRelation getFacets(P basePath, BinaryRelation facetRelation, Var pVar, Set<Expr> effectiveConstraints) {
		ExprTransform exprTransform = new ExprTransformViaPathMapper<>(mapper);
		
		
		
		Set<Element> elements = new LinkedHashSet<>();

		Set<P> paths = new LinkedHashSet<>();
		paths.add(basePath);

		for(Expr expr : effectiveConstraints) {
			Set<P> tmpPaths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor.getPathClass());
			paths.addAll(tmpPaths);
		}
		
		
		for(P p : paths) {
			BinaryRelation br = mapper.getOverallRelation(p);
			elements.addAll(ElementUtils.toElementList(br.getElement()));
		}
			
		for(Expr expr : effectiveConstraints) {
			Expr resolved = ExprTransformer.transform(exprTransform, expr);
			elements.add(new ElementFilter(resolved));
		}
		
		Var s = ((ExprVar)mapper.getExpr(basePath)).asVar();
		
		// Rename all instances of ?p and ?o
		
		Set<Var> forbiddenVars = new HashSet<>();
		for(Element e : elements) {
			Collection<Var> v = PatternVars.vars(e);
			forbiddenVars.addAll(v);
		}
		
		// Rename all instances of 'p' and 'o' variables 
		Set<Var> vars = facetRelation.getVarsMentioned();//new HashSet<>(Arrays.asList(Vars.p, Vars.o));
		vars.remove(facetRelation.getSourceVar());
//		vars.remove(facetRelation.getTargetVar());
		
		Map<Var, Var> rename = VarUtils.createDistinctVarMap(vars, forbiddenVars, true, VarGeneratorBlacklist.create(forbiddenVars));
//		rename.put(facetRelation.getSourceVar(), s);
//		rename.put(s, facetRelation.getSourceVar());
	
		// Rename the source of the facet relation
		Map<Var, Var> r2 = new HashMap<>();
		r2.put(facetRelation.getSourceVar(), s);
	
		facetRelation = facetRelation.applyNodeTransform(new NodeTransformRenameMap(r2));
		
		//s = rename.getOrDefault(s, s);
		
		List<Element> es = new ArrayList<>();
		for(Element e : elements) {
			Element x = ElementUtils.createRenamedElement(e, rename);
			es.add(x);
		}
		
		//boolean isReverse = pathAccessor.isReverse(path);
		//Triple t = QueryFragment.createTriple(isReverse, s, Vars.p, Vars.o);
		es.add(facetRelation.getElement());//ElementUtils.createElement(t));
		
		//BinaryRelation result = new BinaryRelation(ElementUtils.groupIfNeeded(es), Vars.p, Vars.o);
		BinaryRelation result = new BinaryRelationImpl(ElementUtils.groupIfNeeded(es), pVar, facetRelation.getTargetVar());
		
		return result;
	}

	public Map<String, BinaryRelation> getFacets(P path, boolean isReverse) {
		
		// Find all constraints on successor paths
		SetMultimap<P, Expr> childPathToExprs = HashMultimap.create();
		for(Expr expr : constraints) {
			Set<P> paths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor.getPathClass());

			// Check if any parent of the path is the given path
			boolean skipExpr = false;
			for(P candPath : paths) {
				P parentPath = pathAccessor.getParent(candPath);
				boolean candIsReverse = pathAccessor.isReverse(candPath);
				
				// We need to exclude this constraint for the given path
				if(path.equals(parentPath) && isReverse == candIsReverse) {
					skipExpr = true;
					childPathToExprs.put(candPath, expr);
				}
			}

			if(skipExpr) {
				continue;
			}			
		}
		
		
		Map<String, BinaryRelation> result = new HashMap<>();


		Set<Expr> constraintSet = new HashSet<>(constraints);
		Set<P> constrainedChildPaths = childPathToExprs.keySet();
		for(P childPath : constrainedChildPaths) {
			BinaryRelation br = getFacets(childPath, childPathToExprs, constraintSet);
			
			String pStr = pathAccessor.getPredicate(childPath);
			result.put(pStr, br);			
		}
		
		// exclude all predicates that are constrained

		BinaryRelation brr = getRemainingFacets(path, isReverse, constraintSet);

		// Build the constraint to remove all prior properties
		ExprList constrainedPredicates = new ExprList(result.keySet().stream()
				.map(NodeFactory::createURI)
				.map(NodeValue::makeNode)
				.collect(Collectors.toList()));

		if(!constrainedPredicates.isEmpty()) {		
			List<Element> foo = ElementUtils.toElementList(brr.getElement());
			foo.add(new ElementFilter(new E_NotOneOf(new ExprVar(brr.getSourceVar()), constrainedPredicates)));
			brr = new BinaryRelationImpl(ElementUtils.groupIfNeeded(foo), brr.getSourceVar(), brr.getTargetVar());
		}

		result.put(null, brr);
		
		return result;
	}
	
	
	/*
	 * Facet Value Counts are based on a ternary relation:
	 * p, o, and f (focus)
	 * 
	 * (p and o) are part of the same triple pattern (relation?)
	 * 
	 * 
	 * 
	 * root
	 * |- rdf:type
	 *    |- lgdo:Airport 1000
	 *    |- lgdo:BusStop 5000
	 * 
	 * 
	 * 
	 * focus | property | value
	 * 
	 *  
	 * SELECT ?property ?value COUNT(DISTINCT ?focus) {
	 * 
	 * } 
	 * 
	 */
	
	public Map<String, TernaryRelation> getFacetValues(P focusPath, P facetPath, Concept pFilter, Concept oFilter, boolean isReverse) {
		// This is incorrect; we need the values of the facet here;
		// we could take the parent path and restrict it to a set of given predicates
		//pathAccessor.getParent(facetPath);
		Map<String, BinaryRelation> facets = getFacets(facetPath, isReverse);

		// Get the focus element
		BinaryRelation focusRelation = mapper.getOverallRelation(focusPath);
		Set<Element> e1 = new LinkedHashSet<>(ElementUtils.toElementList(focusRelation.getElement()));
		
		Map<String, TernaryRelation> result = new HashMap<>();
		for(Entry<String, BinaryRelation> facet : facets.entrySet()) {
			
			BinaryRelation rel = facet.getValue();
			Set<Element> e2 = new LinkedHashSet<>(ElementUtils.toElementList(rel.getElement()));
			
			Set<Element> e3 = Sets.union(e1, e2);
			Element e4 = ElementUtils.groupIfNeeded(e3);
			TernaryRelation tr = new TernaryRelationImpl(e4, focusRelation.getSourceVar(), rel.getSourceVar(), rel.getTargetVar());
			
			String p = facet.getKey();
			result.put(p, tr);
		}

		return result;
	}
	
	//public TernaryRelation get
	
	public static Concept getFacets(TernaryRelation tr) {
		Concept result = new Concept(tr.getElement(), tr.getP());
		return result;
	}
	
	
	/**
	 * 
	 * 
	 * @return
	 */
	public static TernaryRelation countFacetValues(TernaryRelation tr, int sortDirection) {
		Query query = new Query();
		query.setQuerySelectType();

		query.setQueryPattern(tr.getElement());
		
		Expr agg = query.allocAggregate(new AggCountVarDistinct(new ExprVar(tr.getS())));
		VarExprList vel = query.getProject();
		
		Var p = tr.getP();
		Var o = tr.getO();
		Var c = Vars.c;
		
		vel.add(p);
		vel.add(o);
		vel.add(c, agg);
		query.addGroupBy(p);
		query.addGroupBy(o);
		
		TernaryRelation result = new TernaryRelationImpl(new ElementSubQuery(query), p, o, c);
		
		if(sortDirection != 0) {
			query.addOrderBy(agg, sortDirection);
		}
		
		return result;
	}
	
}