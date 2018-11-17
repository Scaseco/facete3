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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.facete.v3.impl.FacetedBrowsingSessionImpl;
import org.aksw.facete.v3.impl.PathAccessorImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.ExprFragment;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.QueryFragment;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;


public class FacetedQueryGenerator<P> {
	protected Concept baseConcept;
	protected PathToRelationMapper<P> mapper;
	protected Collection<Expr> constraints;
	
	//protected Class<P> pathClass;
	protected PathAccessor<P> pathAccessor;
	

	public static <P> NodeTransform createNodeTransformSubstitutePathReferences(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor) {
		NodeTransform result = PathToRelationMapper.createNodeTransformSubstitutePathReferences(
				pathAccessor::tryMapToPath,
				mapper::getNode);
	return result;
}

//	public NodeTransform createNodeTransformSubstitutePathReferences() {
//		NodeTransform result = PathToRelationMapper.createNodeTransformSubstitutePathReferences(
//				pathAccessor::tryMapToPath,
//				mapper::getNode);
//		return result;
//	}

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

//	public Collection<Expr> getConstraints() {
//		return constraints;
//	}
	
//	public void addConstraint(Expr expr) {
//		Expr rewritten = expr.applyNodeTransform(createNodeTransformSubstitutePathReferences());
//		constraints.add(rewritten);
//	}
	public void addConstraint(Expr expr) {
		constraints.add(expr);
	}

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
	
	
	public SetMultimap<P, Expr> hideConstraintsForPath(SetMultimap<P, Expr> constaintIndex, P path) {
		SetMultimap<P, Expr> result = Multimaps.filterKeys(constaintIndex, k -> !Objects.equals(k, path));
		return result;
//		Set<Expr> excludes = childPathToExprs.get(path);

//		effectiveConstraints = Sets.difference(constraints, excludes);

	}
	
	/**
	 * Creates the relation for the path given as the first argument
	 * 
	 * @param childPath
	 * @param constraintIndex
	 * @param constraints
	 * @param negated
	 * @return
	 */
	public BinaryRelation createRelationForPath(P childPath, SetMultimap<P, Expr> constraintIndex, boolean applySelfConstraints, boolean negated) {
		
		//BinaryRelation rel = pathAccessor.getReachingRelation(childPath);
		BinaryRelation rel = mapper.getOverallRelation(childPath);
		BinaryRelation result;

		if(!rel.isEmpty()) {
			List<Element> elts = new ArrayList<>();
			// TODO If the relation is of form ?s <p> ?o, then rewrite as ?s ?p ?o . FILTER(?p = <p>)

			// FIXME This still breaks - because of conflict between the relation generated for the constraint and for the path


			
			// If the path exists as a constraint DO NOT add it 
			// as it will be added by the constraint
			if(!constraintIndex.containsKey(childPath)) {
				elts.addAll(rel.getElements());
			}

			// NOTE The BIND blocks naive BGP / filter optimization; but a
			// filter placement transform (pushing filters under bind) fixes this
			elts.add(new ElementBind(Vars.p, NodeValue.makeNode(NodeFactory.createURI(pathAccessor.getPredicate(childPath)))));

			
			rel = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), rel.getSourceVar(), rel.getTargetVar());
			
			Multimap<P, Expr> effectiveCi = applySelfConstraints
					? constraintIndex
					: hideConstraintsForPath(constraintIndex, childPath);
			
			P basePath = pathAccessor.getParent(childPath);

			result = createConstraintRelationForPath(basePath, childPath, rel, Vars.p, effectiveCi, negated);
		} else {
			result = rel;
		}
		return result;
	}

	public BinaryRelation getRemainingFacets(P basePath, boolean isReverse, SetMultimap<P, Expr> constraintIndex, boolean negated) {
		BinaryRelation br = new BinaryRelationImpl(
				ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, Vars.p, Vars.o)), Vars.s, Vars.o);
		
		// TODO Combine rel with the constraints
		BinaryRelation rel = mapper.getOverallRelation(basePath);

		BinaryRelation tmp = createConstraintRelationForPath(basePath, null, br, Vars.p, constraintIndex, false);

		
		List<Element> elts = new ArrayList<>();
		elts.addAll(rel.getElements());
		elts.addAll(tmp.getElements());

		BinaryRelation result = new BinaryRelationImpl(
				ElementUtils.groupIfNeeded(elts), tmp.getSourceVar(), tmp.getTargetVar()
		);
		
		return result;
	}

	
	public static <P> Collection<Element> createElementsForExprs(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, Collection<Expr> exprs, boolean negate) {
		NodeTransform nodeTransform = createNodeTransformSubstitutePathReferences(mapper, pathAccessor);
		
		Set<Element> result = new LinkedHashSet<>();
		Set<Expr> resolvedExprs = new LinkedHashSet<>();

		// Collect all mentioned paths so we can getOrCreate their elements
		for(Expr expr : exprs) {
			Set<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath);

			for(P path : paths) {
				BinaryRelation br = mapper.getOverallRelation(path);
			
				result.add(br.getElement());
			}
		}
		
		// Resolve the expression
		for(Expr expr : exprs) {
			
			// TODO We need to add the elements of the paths
			//ExprTransformer.transform(new ExprTransform, expr)
			//Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
			Expr resolved = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);

			resolvedExprs.add(resolved);
		}

		
		Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedExprs);
		
		if(negate) {
			resolvedPathExpr = new E_LogicalNot(resolvedPathExpr);
		}
		
		// Skip adding constraints that equal TRUE
		if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
			result.add(new ElementFilter(resolvedPathExpr));
		}

		//BinaryRelation result = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), br.getSourceVar(), br.getTargetVar());
		return result;
	}
	
	/**
	 * Returns a binary relation with facet - facet value columns.
	 * 
	 * 
	 * @param basePath only used to obtain the connecting variable
	 * @param facetRelation
	 * @param pVar
	 * @param effectiveConstraints
	 * @param negate Negate the constraints - this yields all facet+values unaffected by the effectiveConstraints do NOT apply
	 * @return
	 */
	public BinaryRelation createConstraintRelationForPath(P basePath, P childPath, BinaryRelation facetRelation, Var pVar, Multimap<P, Expr> constraintIndex, boolean negate) {

//		Collection<Element> elts = createElementsFromConstraintIndex(constraintIndex,
//				p -> !negate ? false : (childPath == null ? true : Objects.equals(p, childPath)));

		Collection<Element> elts = createElementsFromConstraintIndex(constraintIndex,
				//p -> Objects.equals(childPath, p),
				p -> !negate ? false : (childPath == null ? true : Objects.equals(p, childPath)));


		//Collection<Element> elts = createElementsForExprs(effectiveConstraints, negate);
		//BinaryRelation tmp = createRelationForPath(facetRelation, effectiveConstraints, negate);
		//List<Element> elts = tmp.getElements();

		
		Var s = (Var)mapper.getNode(basePath);//.asVar();
		
		// Rename all instances of ?p and ?o
		
		Set<Var> forbiddenVars = new HashSet<>();
		for(Element e : elts) {
			Collection<Var> v = PatternVars.vars(e);
			forbiddenVars.addAll(v);
		}
		
		// Rename all instances of 'p' and 'o' variables 
		Set<Var> vars = facetRelation.getVarsMentioned();//new HashSet<>(Arrays.asList(Vars.p, Vars.o));
		vars.remove(facetRelation.getSourceVar());
		vars.remove(facetRelation.getTargetVar());
		
		Map<Var, Var> rename = VarUtils.createDistinctVarMap(vars, forbiddenVars, true, VarGeneratorBlacklist.create(forbiddenVars));
//		rename.put(facetRelation.getSourceVar(), s);
//		rename.put(s, facetRelation.getSourceVar());
	
		// Rename the source of the facet relation
		Map<Var, Var> r2 = new HashMap<>();
		r2.put(facetRelation.getSourceVar(), s);
	
		facetRelation = facetRelation.applyNodeTransform(new NodeTransformRenameMap(r2));
		
		//s = rename.getOrDefault(s, s);
		
		List<Element> es = new ArrayList<>();
		for(Element e : elts) {
			Element x = ElementUtils.createRenamedElement(e, rename);
			es.add(x);
		}
		
		//boolean isReverse = pathAccessor.isReverse(path);
		//Triple t = QueryFragment.createTriple(isReverse, s, Vars.p, Vars.o);
		es.add(facetRelation.getElement());//ElementUtils.createElement(t));
		
		//BinaryRelation result = new BinaryRelation(ElementUtils.groupIfNeeded(es), Vars.p, Vars.o);
		BinaryRelation result = new BinaryRelationImpl(ElementUtils.groupIfNeeded(es), pVar, rename.getOrDefault(facetRelation.getTargetVar(), facetRelation.getTargetVar()));
		
		return result;
	}

	public UnaryRelation createConceptFacets(P path, boolean isReverse, boolean applySelfConstraints, Concept pConstraint) {
		Map<String, BinaryRelation> relations = createMapFacetsAndValues(path, isReverse, false, false);
	
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
				//.filter(e -> !e.isEmpty())
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
		Map<String, BinaryRelation> relations = createMapFacetsAndValues(path, isReverse, false, false);
		BinaryRelation result = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint);
		
		return result;
		//Map<String, TernaryRelation> facetValues = g.getFacetValues(focus, path, false);
	}

	
	
    /** Create helper functions for filtering out the expressions that do not apply for a given path */
	public boolean isExprExcluded(Expr expr, P path, boolean isReverse) {
		boolean result = false;

		// Find all constraints on successor paths
		Set<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath);

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
	public Map<String, BinaryRelation> createMapFacetsAndValues(P path, boolean isReverse, boolean applySelfConstraints) {
		return createMapFacetsAndValues(path, isReverse, applySelfConstraints, false);
	}
	
	public Map<String, BinaryRelation> createMapFacetsAndValues(P basePath, boolean isReverse, boolean applySelfConstraints, boolean negated) {
		
		SetMultimap<P, Expr> constraintIndex = indexConstraints(constraints);

		Map<String, BinaryRelation> result = new HashMap<>();

		Set<P> mentionedPaths = constraintIndex.keySet();
		Set<P> constrainedChildPaths = extractChildPaths(basePath, isReverse, mentionedPaths);
		
		for(P childPath : constrainedChildPaths) {
			BinaryRelation br = createRelationForPath(childPath, constraintIndex, applySelfConstraints, negated);
			
			String pStr = pathAccessor.getPredicate(childPath);
			
			// Substitute the empty predicate by the empty string
			pStr = pStr == null ? "" : pStr;
			
			
			// Skip adding the relation empty string if the relation is empty
			if(!(pStr.isEmpty() && br.isEmpty())) {
				result.put(pStr, br);
			}
		}
		
		// exclude all predicates that are constrained

		BinaryRelation brr = getRemainingFacets(basePath, isReverse, constraintIndex, negated);

		// Build the constraint to remove all prior properties
		ExprList constrainedPredicates = new ExprList(result.keySet().stream()
				.filter(pStr -> !pStr.isEmpty())
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
	
	public TernaryRelation createRelationFacetValue(P focus, P facetPath, boolean isReverse, UnaryRelation pFilter, UnaryRelation oFilter) {
		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse, false);

		List<Element> elements = facetValues.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
				.map(Relation::toTernaryRelation)
				.map(e -> e.joinOn(e.getP()).with(pFilter))
				.map(Relation::getElement)
				.collect(Collectors.toList());

		Element e = ElementUtils.union(elements);

		TernaryRelation result = new TernaryRelationImpl(e, Vars.s, Vars.p, Vars.o);
		return result;
	}

	public TernaryRelation createRelationFacetValues(P focus, P facetPath, boolean isReverse, boolean negated, UnaryRelation pFilter, UnaryRelation oFilter) {
		
		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse, negated);

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
	
	
	/**
	 * Yields a filter expression that excludes all (facet, facetValue) pairs
	 * which are affected by filters.
	 * Conversely, filtering facetAndValues by this expression yields only
	 * those items from which new constraints can be created.
	 * 
	 * 
	 * 
	 * @param facetPath
	 * @param isReverse
	 * @return
	 */
	public ExprFragment getConstraintExpr(P facetPath, boolean isReverse) {
		return null;
	}
	
	
	
	/**
	 * Given a collection of paths, yield those that are a successor of 'basePath' in the direction
	 * specified by 'isReverse'.
	 * 
	 * 
	 * @param basePath
	 * @param isReverse
	 * @param candidates
	 * @return
	 */
	public Set<P> extractChildPaths(P basePath, boolean isReverse, Collection<P> candidates) {
		Set<P> result = new LinkedHashSet<>();
		for(P cand : candidates) {
			P candParent = pathAccessor.getParent(cand);
			// candParent must neither be null nor the root, otherwise isReverse will throw an exception
			if(candParent != null) {
				boolean isCandBwd = pathAccessor.isReverse(cand);
				
				if(isReverse == isCandBwd && Objects.equals(basePath, candParent)) {
					result.add(cand);
				}
			}
		}
		
		return result;
	}

	/**
	 * Indexing of constraints groups those that apply to the same same path(s).
	 * This serves as the base for combination using logical junctors and/or.
	 * 
	 * @param basePath
	 * @param applySelfConstraints
	 * @return
	 */
	public SetMultimap<P, Expr> indexConstraints(Collection<Expr> constraints) {
		
		SetMultimap<P, Expr> result = LinkedHashMultimap.create();
		for(Expr expr : constraints) {
			Set<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath);
			for(P path : paths) {
				result.put(path, expr);
			}
		}

		return result;
	}

	/**
	 * This method is called in the context of creating the element for a path
	 * It enables negating the constraints on that path
	 * 
	 * @param constraintIndex
	 * @return
	 */
	public Set<Element> createElementsFromConstraintIndex(Multimap<P, Expr> constraintIndex,
			//Predicate<? super P> skip,
			Predicate<? super P> negatePath) {

		Set<Element> result = new LinkedHashSet<>();
		for(Entry<P, Collection<Expr>> e : constraintIndex.asMap().entrySet()) {
			P path = e.getKey();

//			boolean skipped = skip == null ? false : skip.test(path);
//			if(skipped) {
//				continue;
//			}
			
			boolean negated = negatePath == null ? false : negatePath.test(path);
			Collection<Expr> exprs = e.getValue();

			
			// The essence of calling createElementsForExprs is combining the exprs with logical or.
			Collection<Element> eltContribs = createElementsForExprs(mapper, pathAccessor, exprs, negated);
			result.addAll(eltContribs);
		}
		
		return result;
	}
	
	public UnaryRelation getConceptForAtPath(P focusPath, P facetPath, boolean applySelfConstraints) {

		SetMultimap<P, Expr> childPathToExprs = indexConstraints(constraints);
		Collection<Element> elts = createElementsFromConstraintIndex(childPathToExprs, null);

		Var resultVar = (Var)mapper.getNode(facetPath);

		P rootPath = getRoot(facetPath, pathAccessor::getParent);
		Var rootVar = (Var)mapper.getNode(rootPath);

		UnaryRelation result = new Concept(ElementUtils.groupIfNeeded(elts), resultVar);
		
		if(baseConcept != null) {
			result = result.joinOn(rootVar).with(baseConcept).toUnaryRelation();
		}
		
		return result;

	}

	// [focus, facet, facetValue]
	public Map<String, TernaryRelation> getFacetValuesCore(P focusPath, P facetPath, UnaryRelation pFilter, UnaryRelation oFilter, boolean isReverse, boolean negated) {
		// This is incorrect; we need the values of the facet here;
		// we could take the parent path and restrict it to a set of given predicates
		//pathAccessor.getParent(facetPath);
		
		boolean applySelfConstraints = true;
		Map<String, BinaryRelation> facets = createMapFacetsAndValues(facetPath, isReverse, applySelfConstraints, negated);

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
	
	/**
	 * Simply create a concept from the predicate column of the ternary relation.
	 * TODO Possibly supersede by using a TernaryRelation.getConceptP() method.
	 * 
	 * 
	 * @param tr
	 * @return
	 */
	public static Concept createConceptFacets(TernaryRelation tr) {
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


// Find all constraints on successor paths
//for(Expr expr : constraints) {
//	if(isExprExcluded(expr, path, isReverse)) {
//		childPathToExprs.put(candPath, expr);
//	}
//}

//for(Expr expr : constraints) {
//	Set<P> paths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor::tryMapToPath);
//
//	// Check if any parent of the path is the given path
//	boolean skipExpr = false;
//	for(P candPath : paths) {
//		P parentPath = pathAccessor.getParent(candPath);
////		if(parentPath != null) {
////			System.out.println(parentPath.toString());
////		}
//		boolean candIsReverse = pathAccessor.isReverse(candPath);
//		
//		// We need to exclude this constraint for the given path
//		if(applySelfConstraints && path.equals(parentPath) && isReverse == candIsReverse) {
//			skipExpr = true;
//			childPathToExprs.put(candPath, expr);
//		}
//	}
//
//	if(skipExpr) {
//		continue;
//	}			
//}


//
//Map<String, BinaryRelation> result = new HashMap<>();
//
//
//Set<Expr> constraintSet = new HashSet<>(constraints);
//Set<P> constrainedChildPaths = childPathToExprs.keySet();
//for(P childPath : constrainedChildPaths) {
//	BinaryRelation br = getFacets(childPath, childPathToExprs, constraintSet);
//	
//	String pStr = pathAccessor.getPredicate(childPath);
//	result.put(pStr, br);			
//}
//
//// exclude all predicates that are constrained
//
//BinaryRelation brr = getRemainingFacets(path, isReverse, constraintSet);
//
//
//
//
//Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, parent, pFilter, null, isReverse);
//
//TernaryRelation r = facetValues.getOrDefault(p, facetValues.get(null));
//
////Var countVar = Vars.c;
////List<Element> elements = facetValues.values().stream()
////		.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
////		.map(Relation::toTernaryRelation)
////		.map(e -> e.joinOn(e.getP()).with(pFilter))
////		.map(e -> FacetedBrowsingSessionImpl.groupBy(e, Vars.s, countVar))
////		.map(Relation::getElement)
////		.collect(Collectors.toList());
////
////
////Element e = ElementUtils.union(elements);
////
////TernaryRelation result = new TernaryRelationImpl(e, Vars.p, Vars.o, countVar);
//
//UnaryRelation result = new Concept(r.getElement(), r.getO());
//
//return result;



//SetMultimap<P, Expr> childPathToExprs = indexConstraints(facetPath, applySelfConstraints);
////activePaths.add(facetPath);
//
//// Assemble the triple patterns for the referenced paths
//
//Set<Element> elts = new LinkedHashSet<>();
////List<Collection<Expr>> exprs = new ArrayList<>();
//
//P rootPath = getRoot(facetPath, pathAccessor::getParent);
//
//Var rootVar = (Var)mapper.getNode(rootPath);
//		
//Var resultVar = (Var)mapper.getNode(facetPath);
//
//BinaryRelation focusRelation = mapper.getOverallRelation(focusPath);
//elts.addAll(ElementUtils.toElementList(focusRelation.getElement()));
//
//NodeTransform nodeTransform = createNodeTransformSubstitutePathReferences();
//
////for(P path : activePaths) {
//for(Entry<P, Collection<Expr>> e : childPathToExprs.asMap().entrySet()) {
//	P path = e.getKey();
//	Collection<Expr> activeExprs = e.getValue();
//	
//	BinaryRelation pathRelation = mapper.getOverallRelation(path);
//
//	// Resolve path references in expressions
//	Set<Expr> resolvedPathExprs = new LinkedHashSet<>();
//	for(Expr expr : activeExprs) {
//		Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
//		resolvedPathExprs.add(resolved);
//	}
//	
//	Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedPathExprs);
//	
//	elts.addAll(ElementUtils.toElementList(pathRelation.getElement()));
//	
//	// Skip adding constraints that equal TRUE
//	if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
//		elts.add(new ElementFilter(resolvedPathExpr));
//	}
//}


//public BinaryRelation getFacetsAndCounts(SPath path, boolean isReverse, Concept pConstraint) {
//	BinaryRelation br = createQueryFacetsAndCounts(path, isReverse, pConstraint);
//	
//	
////	//RelationUtils.attr
////	
////	Query query = RelationUtils.createQuery(br);
////	
////	logger.info("Requesting facet counts: " + query);
////	
////	return ReactiveSparqlUtils.execSelect(() -> conn.query(query))
////		.map(b -> new SimpleEntry<>(b.get(br.getSourceVar()), Range.singleton(((Number)b.get(br.getTargetVar()).getLiteral().getValue()).longValue())));
//}

//// Check if any parent of the path is the given path
//boolean skipExpr = false;
//for(P candPath : paths) {
//	// We need to exclude this constraint for the given path
//	if(!applySelfConstraints && Objects.equals(candPath, basePath)) {
//		skipExpr = true;
//	}
//}
//
//if(!skipExpr) {
//	for(P path : paths) {
////		activePaths.add(path);
//		result.put(path, expr);
//	}
//	//activeExprs.add(expr);
//}


// Add the facetPath to the result if not present already
// This will cause the appropriate triple patterns to be generated
//if(!result.containsKey(basePath)) {
//	result.put(basePath, NodeValue.TRUE);
//}



//Set<Expr> effectiveConstraints;
//Multimaps.filterValues(childPathToExprs, v -> !constraints.contains(v));
//if(!negated) {
//	Set<Expr> excludes = childPathToExprs.get(childPath);
//
//	effectiveConstraints = Sets.difference(constraints, excludes);
//} else {
//	effectiveConstraints = constraints;
//}

