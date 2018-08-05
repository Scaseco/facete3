package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.impl.FacetedBrowsingSessionImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.QueryFragment;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPath;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import jersey.repackaged.com.google.common.collect.Sets;

public class FacetedQueryGenerator<P> {
	protected Concept baseConcept;
	protected PathToRelationMapper<P> mapper;
	protected Collection<Expr> constraints;
	
	//protected Class<P> pathClass;
	protected PathAccessor<P> pathAccessor;
	
	public static <P> NodeTransform createNodeTransformSubstitutePathReferences(
			Function<? super Node, ? extends P> tryMapToPath,
			Function<? super P, ? extends Node> mapToNode) {
		return n -> Optional.ofNullable(
				tryMapToPath.apply(n))
				.map(x -> (Node)mapToNode.apply(x))
				.orElse(n);
	}

	public <P> NodeTransform createNodeTransformSubstitutePathReferences() {
		NodeTransform result = createNodeTransformSubstitutePathReferences(pathAccessor::tryMapToPath, mapper::getNode);
		return result;
	}

//	public Expr substitutePathReferences(Expr expr) {
//		//Set<P> tmpPaths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor::tryMapToPath);
//		NodeTransform t = createNodeTransformSubstitutePathReferences(pathAccessor::tryMapToPath, mapper::getNode);
//		Expr result = expr.applyNodeTransform(t);
//		
//		return result;
//	}
	
	
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
	
//	public void addConstraint(Expr expr) {
//		Expr rewritten = expr.applyNodeTransform(createNodeTransformSubstitutePathReferences());
//		constraints.add(rewritten);
//	}

//	public TernaryRelation createQueryFacetValues(SPath focus, SPath facetPath, boolean isReverse, Concept pFilter, Concept oFilter) {
//		
//		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse);
//
//		Var countVar = Vars.c;
//		List<Element> elements = facetValues.values().stream()
//				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
//				.map(Relation::toTernaryRelation)
//				.map(e -> e.joinOn(e.getP()).with(pFilter))
//				.map(e -> FacetedBrowsingSessionImpl.groupBy(e, Vars.s, countVar))
//				.map(Relation::getElement)
//				.collect(Collectors.toList());
//
//		
//		Element e = ElementUtils.union(elements);
//
//		TernaryRelation result = new TernaryRelationImpl(e, Vars.p, Vars.o, countVar);
//
//		
//		
//		return result;
//
////		FacetedBrowsingSession.align(r, Arrays.asList(Vars.s, Vars.p, Vars.o)
////		List<Relation> aligned = facetValues.values().stream()
////		.map(r -> ))
////		.collect(Collectors.toList());
//
//		
//		
////		Map<String, TernaryRelation> map = facetValues.entrySet().stream()
////		.collect(Collectors.toMap(Entry::getKey, e -> FacetedQueryGenerator.countFacetValues(e.getValue(), -1)));
//		
//	}
	
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

	/**
	 * Returns a binary relation with facet - facet value columns.
	 * 
	 * @param basePath
	 * @param facetRelation
	 * @param pVar
	 * @param effectiveConstraints
	 * @return
	 */
	public BinaryRelation getFacets(P basePath, BinaryRelation facetRelation, Var pVar, Set<Expr> effectiveConstraints) {
		//ExprTransform exprTransform = new ExprTransformViaPathMapper<>(mapper);
		NodeTransform nodeTransform = createNodeTransformSubstitutePathReferences();

		
		
		Set<Element> elements = new LinkedHashSet<>();

		Set<P> paths = new LinkedHashSet<>();
		paths.add(basePath);

		for(Expr expr : effectiveConstraints) {
			Set<P> tmpPaths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor::tryMapToPath);
			paths.addAll(tmpPaths);
		}
		
		
		for(P p : paths) {
			BinaryRelation br = mapper.getOverallRelation(p);
			elements.addAll(ElementUtils.toElementList(br.getElement()));
		}
			
		for(Expr expr : effectiveConstraints) {
			Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
			elements.add(new ElementFilter(resolved));
		}
		
		Var s = (Var)mapper.getNode(basePath);//.asVar();
		
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

	public UnaryRelation createConceptFacets(P path, boolean isReverse, boolean applySelfConstraints, Concept pConstraint) {
		Map<String, BinaryRelation> relations = getFacets(path, isReverse, false);
	
		UnaryRelation result = createConceptFacets(relations, pConstraint);
		return result;
	}
	
	public static UnaryRelation createConceptFacets(Map<String, BinaryRelation> relations, Concept pConstraint) {
		List<Element> elements = relations.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.p, Vars.o)))
				.map(Relation::toBinaryRelation)
				.map(Relation::getElement)
				.collect(Collectors.toList());
		
		Element e = ElementUtils.union(elements);

		UnaryRelation result = new Concept(e, Vars.p);
		//BinaryRelation result = new BinaryRelationImpl(e, Vars.p, countVar);

		return result;
	}

	public static BinaryRelation createRelationFacetsAndCounts(Map<String, BinaryRelation> relations, Concept pConstraint) {
		Var countVar = Var.alloc("__count__");
		List<Element> elements = relations.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.p, Vars.o)))
				.map(Relation::toBinaryRelation)
				.map(e -> e.joinOn(e.getSourceVar()).with(pConstraint))
				.map(e -> FacetedBrowsingSessionImpl.groupBy(e, Vars.o, countVar))
				.map(Relation::getElement)
				.collect(Collectors.toList());
		
		Element e = ElementUtils.union(elements);

		BinaryRelation result = new BinaryRelationImpl(e, Vars.p, countVar);

		return result;
	}


	public BinaryRelation createQueryFacetsAndCounts(P path, boolean isReverse, Concept pConstraint) {
		Map<String, BinaryRelation> relations = getFacets(path, isReverse, false);
		BinaryRelation result = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint);
		
		return result;
		//Map<String, TernaryRelation> facetValues = g.getFacetValues(focus, path, false);
	}

//	public BinaryRelation getFacetsAndCounts(SPath path, boolean isReverse, Concept pConstraint) {
//		BinaryRelation br = createQueryFacetsAndCounts(path, isReverse, pConstraint);
//		
//		
////		//RelationUtils.attr
////		
////		Query query = RelationUtils.createQuery(br);
////		
////		logger.info("Requesting facet counts: " + query);
////		
////		return ReactiveSparqlUtils.execSelect(() -> conn.query(query))
////			.map(b -> new SimpleEntry<>(b.get(br.getSourceVar()), Range.singleton(((Number)b.get(br.getTargetVar()).getLiteral().getValue()).longValue())));
//	}

	
	
    /** Create helper functions for filtering out the expressions that do not apply for a given path */
	public boolean isExprExcluded(Expr expr, P path, boolean isReverse) {
		boolean result = false;

		// Find all constraints on successor paths
		Set<P> paths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor::tryMapToPath);

		// Check if any parent of the path is the given path
		for(P candPath : paths) {
			P parentPath = pathAccessor.getParent(candPath);
			boolean candIsReverse = pathAccessor.isReverse(candPath);
			
			// We need to exclude this constraint for the given path
			if(path.equals(parentPath) && isReverse == candIsReverse) {
				result = true;
			}
		}

		return result;
	}
//	
	/**
	 * For the give path and direction, yield a map of binary relations for the corresponding facets.
	 * An entry with key null indicates the predicate / distinct value count pairs with all constraints in place
	 * 
	 * @param path
	 * @param isReverse
	 * @return
	 */
	public Map<String, BinaryRelation> getFacets(P path, boolean isReverse, boolean applySelfConstraints) {
		
		// Find all constraints on successor paths
		SetMultimap<P, Expr> childPathToExprs = HashMultimap.create();
//		for(Expr expr : constraints) {
//			if(isExprExcluded(expr, path, isReverse)) {
//				childPathToExprs.put(candPath, expr);
//			}
//		}
		
		for(Expr expr : constraints) {
			Set<P> paths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor::tryMapToPath);

			// Check if any parent of the path is the given path
			boolean skipExpr = false;
			for(P candPath : paths) {
				P parentPath = pathAccessor.getParent(candPath);
				boolean candIsReverse = pathAccessor.isReverse(candPath);
				
				// We need to exclude this constraint for the given path
				if(applySelfConstraints && path.equals(parentPath) && isReverse == candIsReverse) {
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
	
	public TernaryRelation getFacetValues(P focus, P facetPath, boolean isReverse, UnaryRelation pFilter, UnaryRelation oFilter) {
		
		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse);

		Var countVar = Vars.c;
		List<Element> elements = facetValues.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
				.map(Relation::toTernaryRelation)
				.map(e -> e.joinOn(e.getP()).with(pFilter))
				.map(e -> FacetedBrowsingSessionImpl.groupBy(e, Vars.s, countVar))
				.map(Relation::getElement)
				.collect(Collectors.toList());

		
		Element e = ElementUtils.union(elements);

		TernaryRelation result = new TernaryRelationImpl(e, Vars.p, Vars.o, countVar);

		
		
		return result;
	}
	
	// TODO Move to TreeUtils
	public static <T> T getRoot(T item, Function<? super T, ? extends T> getParent) {
		T result = null;
		while(item != null) {
			result = item;
			item = getParent.apply(result);
		}

		return result;
	}

	public UnaryRelation getConceptForAtPath(P focusPath, P facetPath, boolean applySelfConstraints) {
		
//		P parent = pathAccessor.getParent(facetPath);
//		boolean isReverse = pathAccessor.isReverse(facetPath);
//		String p = pathAccessor.getPredicate(facetPath);
//		Concept pFilter = new Concept(new ElementFilter(new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(NodeFactory.createURI(p)))), Vars.p);
//		
//		
//		
		// Find all constraints on successor paths
		Set<P> activePaths = new LinkedHashSet<>();
		Set<Expr> activeExprs = new LinkedHashSet<>();
		//SetMultimap<P, Expr> childPathToExprs = HashMultimap.create();
		for(Expr expr : constraints) {
			Set<P> paths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor::tryMapToPath);
			
			// Check if any parent of the path is the given path
			boolean skipExpr = false;
			for(P candPath : paths) {
				// We need to exclude this constraint for the given path
				if(!applySelfConstraints && Objects.equals(candPath, facetPath)) {
					skipExpr = true;
				}
			}

			if(!skipExpr) {
				for(P path : paths) {
					activePaths.add(path);
				}
				activeExprs.add(expr);
			}
		}
		
		activePaths.add(facetPath);
		
		// Assemble the triple patterns for the referenced paths
		
		Set<Element> elts = new LinkedHashSet<>();
		//List<Collection<Expr>> exprs = new ArrayList<>();

		P rootPath = getRoot(facetPath, pathAccessor::getParent);
		
		Var rootVar = (Var)mapper.getNode(rootPath);
				
		Var resultVar = (Var)mapper.getNode(facetPath);

		BinaryRelation focusRelation = mapper.getOverallRelation(focusPath);
		elts.addAll(ElementUtils.toElementList(focusRelation.getElement()));
		
		for(P path : activePaths) {
			BinaryRelation pathRelation = mapper.getOverallRelation(path);
			elts.addAll(ElementUtils.toElementList(pathRelation.getElement()));
		}	
				
		NodeTransform nodeTransform = createNodeTransformSubstitutePathReferences();

		for(Expr expr : activeExprs) {
			Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
			elts.add(new ElementFilter(resolved));
		}

		UnaryRelation result = new Concept(ElementUtils.groupIfNeeded(elts), resultVar);
		
		if(baseConcept != null) {
			result = result.joinOn(rootVar).with(baseConcept).toUnaryRelation();
		}
		
		return result;
//		
//		Map<String, BinaryRelation> result = new HashMap<>();
//
//
//		Set<Expr> constraintSet = new HashSet<>(constraints);
//		Set<P> constrainedChildPaths = childPathToExprs.keySet();
//		for(P childPath : constrainedChildPaths) {
//			BinaryRelation br = getFacets(childPath, childPathToExprs, constraintSet);
//			
//			String pStr = pathAccessor.getPredicate(childPath);
//			result.put(pStr, br);			
//		}
//		
//		// exclude all predicates that are constrained
//
//		BinaryRelation brr = getRemainingFacets(path, isReverse, constraintSet);
//
//		
//		
//		
//		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, parent, pFilter, null, isReverse);
//
//		TernaryRelation r = facetValues.getOrDefault(p, facetValues.get(null));
//		
////		Var countVar = Vars.c;
////		List<Element> elements = facetValues.values().stream()
////				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
////				.map(Relation::toTernaryRelation)
////				.map(e -> e.joinOn(e.getP()).with(pFilter))
////				.map(e -> FacetedBrowsingSessionImpl.groupBy(e, Vars.s, countVar))
////				.map(Relation::getElement)
////				.collect(Collectors.toList());
////
////		
////		Element e = ElementUtils.union(elements);
////
////		TernaryRelation result = new TernaryRelationImpl(e, Vars.p, Vars.o, countVar);
//
//		UnaryRelation result = new Concept(r.getElement(), r.getO());
//		
//		return result;
	}

	// [focus, facet, facetValue]
	public Map<String, TernaryRelation> getFacetValuesCore(P focusPath, P facetPath, UnaryRelation pFilter, UnaryRelation oFilter, boolean isReverse) {
		// This is incorrect; we need the values of the facet here;
		// we could take the parent path and restrict it to a set of given predicates
		//pathAccessor.getParent(facetPath);
		Map<String, BinaryRelation> facets = getFacets(facetPath, isReverse, false);

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