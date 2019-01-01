package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.facete.v3.impl.ConstraintFacadeImpl;
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
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Equals;
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
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.QueryFragment;

import com.google.common.collect.ComparisonChain;
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
	
	public static <P> BinaryRelation createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
		BinaryRelation result;
		if(includeAbsent) {

			P parent = pathAccessor.getParent(childPath);
			
			// Somewhat hacky: First create the overall path in order to
			// allocate variables in the mapper
			BinaryRelation tmp = mapper.getOverallRelation(childPath);

			// But actually we only need the path to the parent first
			BinaryRelation br = mapper.getOverallRelation(parent);
			
			// We need to adjust the variable naming of the last step
			// according to the mapper's state, so rename the variables
			BinaryRelation rawLastStep = pathAccessor.getReachingRelation(childPath);

			
			// TODO Wrap this renaming construct up in the API
			BinaryRelation helper = new BinaryRelationImpl(new ElementGroup(), br.getTargetVar(), tmp.getTargetVar());
			BinaryRelation lastStep = helper.joinOn(helper.getSourceVar(), helper.getTargetVar())
					.with(rawLastStep)
					.toBinaryRelation();
			
				
			Collection<Element> elts = new ArrayList<>();
			elts.addAll(br.getElements());
			elts.add(new ElementOptional(lastStep.getElement()));
			//elts.add(new ElementFilter(new E_LogicalNot(new E_Bound(new ExprVar(lastStep.getTargetVar())))));
			
			Element group = ElementUtils.groupIfNeeded(elts);
			
			result = new BinaryRelationImpl(group, tmp.getSourceVar(), lastStep.getTargetVar());
			
		} else {
			result = mapper.getOverallRelation(childPath);
		}
		return result;
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
	public BinaryRelation createRelationForPath(P childPath, SetMultimap<P, Expr> constraintIndex, boolean applySelfConstraints, boolean negated, boolean includeAbsent) {
		
		//BinaryRelation rel = pathAccessor.getReachingRelation(childPath);
		BinaryRelation rel = createRelationForPath(mapper, pathAccessor, childPath, includeAbsent);//mapper.getOverallRelation(childPath);
		BinaryRelation result;

		if(!rel.isEmpty()) {
			List<Element> elts = new ArrayList<>();
			// TODO If the relation is of form ?s <p> ?o, then rewrite as ?s ?p ?o . FILTER(?p = <p>)

			// FIXME This still breaks - because of conflict between the relation generated for the constraint and for the path


			Multimap<P, Expr> effectiveCi = applySelfConstraints
					? constraintIndex
					: hideConstraintsForPath(constraintIndex, childPath);
			
			// If the path exists as a constraint DO NOT add it 
			// as it will be added by the constraint
			if(!effectiveCi.containsKey(childPath)) {
				elts.addAll(rel.getElements());
			}

			// NOTE The BIND blocks naive BGP / filter optimization; but a
			// filter placement transform (pushing filters under bind) fixes this
			elts.add(new ElementBind(Vars.p, NodeValue.makeNode(NodeFactory.createURI(pathAccessor.getPredicate(childPath)))));

			
			rel = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), rel.getSourceVar(), rel.getTargetVar());
			
			
			P basePath = pathAccessor.getParent(childPath);

			result = createConstraintRelationForPath(basePath, childPath, rel, Vars.p, effectiveCi, negated, includeAbsent);
		} else {
			result = rel;
		}
		return result;
	}

	public BinaryRelation getRemainingFacets(P focusPath, P facetOriginPath, boolean isReverse, SetMultimap<P, Expr> constraintIndex, boolean negated, boolean includeAbsent) {
		BinaryRelation result = includeAbsent
				? getRemainingFacetsWithAbsent(focusPath, facetOriginPath, isReverse, constraintIndex, negated)
				: getRemainingFacetsWithoutAbsent(facetOriginPath, isReverse, constraintIndex, negated, includeAbsent);
				
		return result;
	}


	public BinaryRelation getRemainingFacetsWithAbsent(P focusPath, P facetOriginPath, boolean isReverse, SetMultimap<P, Expr> constraintIndex, boolean negated) {
		boolean includeAbsent = true;
		
		Element tripleEl = ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, Vars.p, Vars.o));
		Element baseEl = new ElementOptional(tripleEl);

		BinaryRelation br = new BinaryRelationImpl(baseEl, Vars.s, Vars.o);


		// TODO Combine rel with the constraints
		BinaryRelation rel = mapper.getOverallRelation(facetOriginPath);

		BinaryRelation tmp = createConstraintRelationForPath(facetOriginPath, null, br, Vars.p, constraintIndex, false, includeAbsent);

		
		List<Element> elts = new ArrayList<>();
		
		// In this case we need to inject the set of facets:
		// so that we can left join the focus resources

		// We are only interested in the null entry here which denotes the set of
		// unconstraint facets
		// Te constraint facets are properly processed individually
		//Map<String, BinaryRelation> rawRelations = createMapFacetsAndValues(facetOriginPath, isReverse, false, false, false);
		
		Map<String, TernaryRelation> rawRelations3 = getFacetValuesCore(focusPath, facetOriginPath, null, null, isReverse, negated, false, false);

		TernaryRelation tr = rawRelations3.get(null);
		UnaryRelation rawFacetConcept = tr.project(tr.getP()).toUnaryRelation();
		
//		Map<String, TernaryRelation> rawRelation = Collections.singletonMap(null, rawRelations.get(null).));
		//Map<String, TernaryRelation> relations = Collections.singletonMap(null, rawBr);
		
		//UnaryRelation rawFacetConcept = createConceptFacets(relations, null);
		
		//UnaryRelation rawFacetConcept = createConceptFacets(facetOriginPath, isReverse, false, null);

		// This should make all variables of the facet concept 
		// - except for ?p - distinct from the tmp
		UnaryRelation facetConcept = rawFacetConcept.rename(varName -> "opt_" + varName, Vars.p).toUnaryRelation();
		
		//UnaryRelation facetConcept = rawFacetConcept.joinOn(Vars.p).yieldRenamedFilter(rawFacetConcept).toUnaryRelation();

		elts.addAll(facetConcept.getElements());
		
		
		elts.addAll(rel.getElements());
		elts.addAll(tmp.getElements());

		BinaryRelation result = new BinaryRelationImpl(
				ElementUtils.groupIfNeeded(elts), tmp.getSourceVar(), tmp.getTargetVar()
		);
		
		return result;
	}
	
	public BinaryRelation getRemainingFacetsWithoutAbsent(P facetOriginPath, boolean isReverse, SetMultimap<P, Expr> constraintIndex, boolean negated, boolean includeAbsent) {

		Element baseEl = ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, Vars.p, Vars.o));

		BinaryRelation br = new BinaryRelationImpl(baseEl, Vars.s, Vars.o);

		// TODO Combine rel with the constraints
		BinaryRelation rel = mapper.getOverallRelation(facetOriginPath);
		BinaryRelation tmp = createConstraintRelationForPath(facetOriginPath, null, br, Vars.p, constraintIndex, false, includeAbsent);
		
		List<Element> elts = new ArrayList<>();

		elts.addAll(rel.getElements());
		elts.addAll(tmp.getElements());

		BinaryRelation result = new BinaryRelationImpl(
				ElementUtils.groupIfNeeded(elts), tmp.getSourceVar(), tmp.getTargetVar()
		);
		
		return result;
	}

	
	public static boolean containsAbsent(Collection<? extends Expr> exprs) {
		boolean result = exprs.stream().anyMatch(FacetedQueryGenerator::isAbsent);
		return result;
	}

	public static int compareAbsent(Expr a, Expr b) {
		int result = ComparisonChain.start()
				.compareFalseFirst(isAbsent(a), isAbsent(b))
				.result();
		return result;
	}

	public static int compareAbsent(Collection<? extends Expr> a, Collection<? extends Expr> b) {
		int result = ComparisonChain.start()
				.compareFalseFirst(containsAbsent(a), containsAbsent(b))
				.result();
		return result;
	}

	public static boolean isAbsent(Expr expr) {
		boolean result;
		if(expr instanceof E_Equals) {
			E_Equals e = (E_Equals)expr;
			result = e.getArg2().equals(ConstraintFacadeImpl.NV_ABSENT);
		} else {
			result = false;
		}
		
		return result;
	}

	
	public static Expr internalRewriteAbsent(Expr expr) {
		return new E_LogicalNot(new E_Bound(expr.getFunction().getArgs().get(0)));
	}
//	
//	
	public static <P> Map<P, BinaryRelation> allocatePathRelations(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, Multimap<P, Expr> constraintIndex) {
		Map<P, BinaryRelation> result = new LinkedHashMap<>();
		
		for(Entry<P, Collection<Expr>> e : constraintIndex.asMap().entrySet()) {
//			llCollection<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath).values();
//
//			// FIXME And another issue:
//			// Element creation for absent-constrainted paths has be done on the path level
//			// Presently we operate expr-centric: whenever an expr references a Path we
//			// create the path's element.

			P path = e.getKey();
			Collection<Expr> exprs = e.getValue();
			// Deal with absent values
			boolean containsAbsent = containsAbsent(exprs);
			BinaryRelation br = createRelationForPath(mapper, pathAccessor, path, containsAbsent);

			result.put(path, br);

//			if(containsAbsent) {
//			
//				P parent = pathAccessor.getParent(path);
//				
//				// Somewhat hacky: First create the overall path in order to
//				// allocate variables in the mapper
//				BinaryRelation tmp = mapper.getOverallRelation(path);
//
//				// But actually we only need the path to the parent first
//				BinaryRelation br = mapper.getOverallRelation(parent);
//				
//				// We need to adjust the variable naming of the last step
//				// according to the mapper's state, so rename the variables
//				BinaryRelation rawLastStep = pathAccessor.getReachingRelation(path);
//
//				
//				// TODO Wrap this renaming construct up in the API
//				BinaryRelation helper = new BinaryRelationImpl(new ElementGroup(), br.getTargetVar(), tmp.getTargetVar());
//				BinaryRelation lastStep = helper.joinOn(helper.getSourceVar(), helper.getTargetVar())
//						.with(rawLastStep)
//						.toBinaryRelation();
//				
//					
//				Collection<Element> elts = new ArrayList<>();
//				elts.addAll(br.getElements());
//				elts.add(new ElementOptional(lastStep.getElement()));
//				//elts.add(new ElementFilter(new E_LogicalNot(new E_Bound(new ExprVar(lastStep.getTargetVar())))));
//				
//				Element group = ElementUtils.groupIfNeeded(elts);
//				
//				BinaryRelation newBr = new BinaryRelationImpl(group, tmp.getSourceVar(), lastStep.getTargetVar());
//				
//				result.put(path, newBr);
//////				result.put(key, value)
//////				
//////				// Wrapping an OPTIONAL with a group changes the semantics :/
//////				result.addAll(elts);
////				//result.add(e);
////
////				//Expr resolved = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);
////
////				resolvedExprs.add(new E_LogicalNot(new E_Bound(new ExprVar(lastStep.getTargetVar()))));
////				//System.out.println(resolved);
////				
//////				throw new RuntimeException("Not supported yet");
//			} else {
//				BinaryRelation br = mapper.getOverallRelation(path);
//
//				result.put(path, br);
//			}
		}
		
		return result;
	}
	
	
	/**
	 * Creates elements from the given constraint expressions.
	 * This method collects all paths referenced in the expressions and
	 * allocates their relations.
	 * 
	 * 
	 * Used by DataQueryImpl::filter 
	 * 
	 * @param mapper
	 * @param pathAccessor
	 * @param constraints
	 * @param negate
	 * @return
	 */
	public static <P> Collection<Element> createElementsForExprs(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, Collection<Expr> constraints, boolean negate) {
		
		SetMultimap<P, Expr> constraintIndex = indexConstraints(pathAccessor, constraints);
		Map<P, BinaryRelation> pathToRelation = allocatePathRelations(mapper, pathAccessor, constraintIndex);
		
		Collection<Element> result = createElementsForExprs(mapper, pathAccessor, pathToRelation, constraints, negate);

		return result;
	}	
	
	public static <P> Collection<Element> createElementsForExprs(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, Map<P, BinaryRelation> pathToRelation, Collection<Expr> baseExprs, boolean negate) {
		
		NodeTransform nodeTransform = createNodeTransformSubstitutePathReferences(mapper, pathAccessor);
		
		Set<Element> result = new LinkedHashSet<>();
		Set<Expr> resolvedExprs = new LinkedHashSet<>();

		// Sort base exprs - absent ones last
		List<Expr> tmp = baseExprs.stream()
				.map(e -> isAbsent(e) ? internalRewriteAbsent(e) : e)
				.collect(Collectors.toList());
		
		List<Expr> exprs = new ArrayList<>(tmp);
		Collections.sort(exprs, FacetedQueryGenerator::compareAbsent);
		
		// Collect all mentioned paths so we can getOrCreate their elements
		
		
		for(Expr expr : exprs) {
			Collection<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath).values();
			for(P path : paths) {
				BinaryRelation br = pathToRelation.get(path);
				result.addAll(br.getElements());
			}
		}
		
//		Set<Expr> skipExprs = new HashSet<>();
//		for(Expr expr : exprs) {
//			Collection<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath).values();

			// FIXME And another issue:
			// Element creation for absent-constrainted paths has be done on the path level
			// Presently we operate expr-centric: whenever an expr references a Path we
			// create the path's element.

			// Deal with absent values
//			if(isAbsent(expr)) {
//			
//				skipExprs.add(expr);
//
//				P path = paths.iterator().next();
//				P parent = pathAccessor.getParent(path);
//				
//				// Somewhat hacky: First create the overall path in order to
//				// allocate variables in the mapper
//				BinaryRelation tmp = mapper.getOverallRelation(path);
//
//				// But actually we only need the path to the parent first
//				BinaryRelation br = mapper.getOverallRelation(parent);
//				
//				// We need to adjust the variable naming of the last step
//				// according to the mapper's state, so rename the variables
//				BinaryRelation rawLastStep = pathAccessor.getReachingRelation(path);
//
//				
//				// TODO Wrap this renaming construct up in the API
//				BinaryRelation helper = new BinaryRelationImpl(new ElementGroup(), br.getTargetVar(), tmp.getTargetVar());
//				BinaryRelation lastStep = helper.joinOn(helper.getSourceVar(), helper.getTargetVar())
//						.with(rawLastStep)
//						.toBinaryRelation();
//				
//					
//				Collection<Element> elts = new ArrayList<>();
//				elts.addAll(br.getElements());
//				elts.add(new ElementOptional(lastStep.getElement()));
//				//elts.add(new ElementFilter(new E_LogicalNot(new E_Bound(new ExprVar(lastStep.getTargetVar())))));
//				
//				//Element e = ElementUtils.groupIfNeeded(elts);
//				
//				//BinaryRelation newBr = new BinaryRelationImpl(e, tmp.getSourceVar(), lastStep.getTargetVar());
//				
//				// Wrapping an OPTIONAL with a group changes the semantics :/
//				result.addAll(elts);
//				//result.add(e);
//
//				//Expr resolved = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);
//
//				resolvedExprs.add(new E_LogicalNot(new E_Bound(new ExprVar(lastStep.getTargetVar()))));
//				//System.out.println(resolved);
//				
////				throw new RuntimeException("Not supported yet");
//			} else {
//				
//				for(P path : paths) {
//					BinaryRelation br = mapper.getOverallRelation(path);
//				
//					result.add(br.getElement());
//				}
//			}
//		}
		
		// Resolve the expression
		for(Expr expr : exprs) {
			
			// TODO We need to add the elements of the paths
			//ExprTransformer.transform(new ExprTransform, expr)
			//Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
			Expr resolved = ExprTransformer.transform(new NodeTransformExpr(nodeTransform), expr);

			resolvedExprs.add(resolved);
		}

		
		Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedExprs);
		
		if(resolvedPathExpr != null) {
			if(negate) {
				resolvedPathExpr = new E_LogicalNot(resolvedPathExpr);
			}
			
			// Skip adding constraints that equal TRUE
			if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
				result.add(new ElementFilter(resolvedPathExpr));
			}
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
	public BinaryRelation createConstraintRelationForPath(P basePath, P childPath, BinaryRelation facetRelation, Var pVar, Multimap<P, Expr> constraintIndex, boolean negate, boolean includeAbsent) {

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

	public UnaryRelation createConceptFacets(P facetOriginPath, boolean isReverse, boolean applySelfConstraints, Concept pConstraint) {
		Map<String, BinaryRelation> relations = createMapFacetsAndValues(null, facetOriginPath, isReverse, false, false, false);
	
		UnaryRelation result = createConceptFacets(relations, pConstraint);
		return result;
	}
	
	public static UnaryRelation createConceptFacets(Map<String, BinaryRelation> relations, Concept pConstraint) {
		List<Element> elements = relations.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.p, Vars.o)))
				.map(Relation::toBinaryRelation)
				.map(Relation::getElement)
				.collect(Collectors.toList());
		
		Element e = ElementUtils.unionIfNeeded(elements);

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
		
		Element e = ElementUtils.unionIfNeeded(elements);

		BinaryRelation result = new BinaryRelationImpl(e, Vars.p, countVar);

		return result;
	}


	@Deprecated // Does not seem to be used / undeprecate if this is wrong
	public BinaryRelation createQueryFacetsAndCounts(P facetOriginPath, boolean isReverse, Concept pConstraint) {
		Map<String, BinaryRelation> relations = createMapFacetsAndValues(null, facetOriginPath, isReverse, false, false, false);
		BinaryRelation result = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint);
		
		return result;
		//Map<String, TernaryRelation> facetValues = g.getFacetValues(focus, path, false);
	}

	
	
    /** Create helper functions for filtering out the expressions that do not apply for a given path */
	@Deprecated // Does not seem to be used / undeprecate if this is wrong
	public boolean isExprExcluded(Expr expr, P path, boolean isReverse) {
		boolean result = false;

		// Find all constraints on successor paths
		Collection<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath).values();

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
	 * Version without focus path
	 * 
	 * @param facetOriginPath
	 * @param isReverse
	 * @param applySelfConstraints
	 * @return
	 */
	@Deprecated
	public Map<String, BinaryRelation> createMapFacetsAndValues(P facetOriginPath, boolean isReverse, boolean applySelfConstraints) {
		Map<String, BinaryRelation> result = createMapFacetsAndValues(null, facetOriginPath, isReverse, applySelfConstraints);
		return result;
	}

	/**
	 * For the give path and direction, yield a map of binary relations for the corresponding facets.
	 * An entry with key null indicates the predicate / distinct value count pairs with all constraints in place
	 * 
	 * @param facetOriginPath
	 * @param isReverse
	 * @return
	 */
	public Map<String, BinaryRelation> createMapFacetsAndValues(P focusPath, P facetOriginPath, boolean isReverse, boolean applySelfConstraints) {
		return createMapFacetsAndValues(focusPath, facetOriginPath, isReverse, applySelfConstraints, false, false);
	}
	
	public Map<String, BinaryRelation> createMapFacetsAndValues(P focusPath, P facetOriginPath, boolean isReverse, boolean applySelfConstraints, boolean negated, boolean includeAbsent) {
		
		SetMultimap<P, Expr> constraintIndex = indexConstraints(pathAccessor, constraints);

		Map<String, BinaryRelation> result = new HashMap<>();

		Set<P> mentionedPaths = constraintIndex.keySet();
		Set<P> constrainedChildPaths = extractChildPaths(facetOriginPath, isReverse, mentionedPaths);
		
		for(P childPath : constrainedChildPaths) {
			BinaryRelation br = createRelationForPath(childPath, constraintIndex, applySelfConstraints, negated, includeAbsent);
			
			String pStr = pathAccessor.getPredicate(childPath);
			
			// Substitute the empty predicate by the empty string
			// The empty string predicate (zero length path) is different from
			// the set of remaining predicates indicated by a null entry in the result map
			pStr = pStr == null ? "" : pStr;
			
			
			// Skip adding the relation empty string if the relation is empty
			if(!(pStr.isEmpty() && br.isEmpty())) {
				result.put(pStr, br);
			}
		}
		
		// exclude all predicates that are constrained

		BinaryRelation brr = getRemainingFacets(focusPath, facetOriginPath, isReverse, constraintIndex, negated, includeAbsent);

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
	
	
	// TODO This method differs from the other one only by a single line that does
	// a group by for counting
	// we should streamline this
	public TernaryRelation createRelationFacetValue(P focus, P facetPath, boolean isReverse, UnaryRelation pFilter, UnaryRelation oFilter, boolean applySelfConstraints, boolean includeAbsent) {
		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse, false, applySelfConstraints, includeAbsent);

		List<Element> elements = facetValues.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
				.map(Relation::toTernaryRelation)
				.map(e -> e.joinOn(e.getP()).with(pFilter))
				.map(Relation::getElement)
				.collect(Collectors.toList());

		Element e = ElementUtils.unionIfNeeded(elements);

		TernaryRelation result = new TernaryRelationImpl(e, Vars.s, Vars.p, Vars.o);
		return result;
	}

	public TernaryRelation createRelationFacetValues(P focus, P facetPath, boolean isReverse, boolean negated, UnaryRelation pFilter, UnaryRelation oFilter, boolean includeAbsent) {
		
		Map<String, TernaryRelation> facetValues = getFacetValuesCore(focus, facetPath, pFilter, oFilter, isReverse, negated, false, includeAbsent);

		Var countVar = Vars.c;
		List<Element> elements = facetValues.values().stream()
				.map(e -> FacetedBrowsingSessionImpl.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
				.map(Relation::toTernaryRelation)
				.map(e -> e.joinOn(e.getP()).with(pFilter))
				.map(e -> FacetedBrowsingSessionImpl.groupBy(e, Vars.s, countVar))
				.map(Relation::getElement)
				.collect(Collectors.toList());

		
		Element e = ElementUtils.unionIfNeeded(elements);

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
	 * Given a collection of paths, yield those that are a direct successor of 'basePath'
	 * in the direction specified by 'isReverse'.
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
	public static <P> SetMultimap<P, Expr> indexConstraints(PathAccessor<P> pathAccessor, Collection<Expr> constraints) {
		
		SetMultimap<P, Expr> result = LinkedHashMultimap.create();
		for(Expr expr : constraints) {
			Collection<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath).values();
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
		
		
		// Sort paths last that have constraints with an optional component (e.g. absent)
		
		Map<P, Collection<Expr>> map = constraintIndex.asMap();
		List<P> order = new ArrayList<>(constraintIndex.keySet());
		Collections.sort(order, (a, b) -> compareAbsent(map.get(a), map.get(b)));


		// Map every path to its relation element
		// We rely here on the constraintIndex containing entries for ALL referenced paths
		Map<P, BinaryRelation> pathToRelation = allocatePathRelations(mapper, pathAccessor, constraintIndex);

		//for(Entry<P, Collection<Expr>> e : constraintIndex.asMap().entrySet()) {
		for(P path : order) {
			Collection<Expr> exprs = map.get(path);
			
			
			//P path = e.getKey();

//			boolean skipped = skip == null ? false : skip.test(path);
//			if(skipped) {
//				continue;
//			}
			
			boolean negated = negatePath == null ? false : negatePath.test(path);
//			Collection<Expr> exprs = e.getValue();

			
			// The essence of calling createElementsForExprs is combining the exprs with logical or.
			Collection<Element> eltContribs = createElementsForExprs(mapper, pathAccessor, pathToRelation, exprs, negated);
			result.addAll(eltContribs);
		}
		
		return result;
	}

//	public UnaryRelation getConceptForAtPath(P focusPath, P facetPath, boolean applySelfConstraints) {
//		UnaryRelation result = createRelationFacetValue(focusPath, facetPath, false, null, null, applySelfConstraints)
//			.project(Vars.o).toUnaryRelation();
//		
//		return result;
//	}

	public UnaryRelation getConceptForAtPath(P focusPath, P facetPath, boolean applySelfConstraints) {

		SetMultimap<P, Expr> rawIndex = indexConstraints(pathAccessor, constraints);
		
		SetMultimap<P, Expr> childPathToExprs = applySelfConstraints
				? rawIndex
				: hideConstraintsForPath(rawIndex, facetPath);
		
		Collection<Element> elts = createElementsFromConstraintIndex(childPathToExprs, null);

//		xxx
//		BinaryRelation br = createRelationForPath(childPath, constraintIndex, applySelfConstraints, negated);
		{
			// FIXME Not sure whether this is the right place to add focus / facet-path elements;
			// can't we 
			BinaryRelation focusRelation = mapper.getOverallRelation(focusPath);
			//Set<Element> tmp = new LinkedHashSet<>();
			elts.addAll(focusRelation.getElements());
			
			BinaryRelation facetRelation = mapper.getOverallRelation(facetPath);
			//Set<Element> tmp = new LinkedHashSet<>();
			elts.addAll(facetRelation.getElements());
		}
		
		
		
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
	public Map<String, TernaryRelation> getFacetValuesCore(P focusPath, P facetPath, UnaryRelation pFilter, UnaryRelation oFilter, boolean isReverse, boolean negated, boolean applySelfConstraints, boolean includeAbsent) {
		// This is incorrect; we need the values of the facet here;
		// we could take the parent path and restrict it to a set of given predicates
		//pathAccessor.getParent(facetPath);
		
		//boolean applySelfConstraints = false;
		Map<String, BinaryRelation> facets = createMapFacetsAndValues(focusPath, facetPath, isReverse, applySelfConstraints, negated, includeAbsent);

		// Get the focus element
		BinaryRelation focusRelation = mapper.getOverallRelation(focusPath);
		Set<Element> e1 = new LinkedHashSet<>(ElementUtils.toElementList(focusRelation.getElement()));
		
		Map<String, TernaryRelation> result = new HashMap<>();
		for(Entry<String, BinaryRelation> facet : facets.entrySet()) {
			
			BinaryRelation rel = facet.getValue();
			Set<Element> e2 = new LinkedHashSet<>(ElementUtils.toElementList(rel.getElement()));
			
			Set<Element> e3 = Sets.union(e1, e2);
			Element e4 = ElementUtils.groupIfNeeded(e3);
			TernaryRelation tr = new TernaryRelationImpl(e4, focusRelation.getTargetVar(), rel.getSourceVar(), rel.getTargetVar());
			
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

