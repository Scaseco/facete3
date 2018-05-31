package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Dimension;
import org.hobbit.benchmark.faceted_browsing.v2.domain.DimensionImpl;
import org.hobbit.benchmark.faceted_browsing.v2.domain.ExprPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.ExprTransformViaPathMapper;
import org.hobbit.benchmark.faceted_browsing.v2.domain.FactoryWithModel;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessor;
import org.hobbit.benchmark.faceted_browsing.v2.domain.PathAccessorSPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.QueryFragment;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPath;
import org.hobbit.benchmark.faceted_browsing.v2.domain.SPathImpl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

import jersey.repackaged.com.google.common.collect.Sets;


class FacetedQueryGenerator<P> {
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
		
		rel = new BinaryRelation(ElementUtils.groupIfNeeded(elts), rel.getSourceVar(), rel.getTargetVar());
		
		BinaryRelation result = getFacets(pathAccessor.getParent(childPath), rel, Vars.p, effectiveConstraints);
		return result;
	}

	public BinaryRelation getRemainingFacets(P basePath, boolean isReverse, Set<Expr> effectiveConstraints) {
		BinaryRelation br = new BinaryRelation(
				ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, Vars.p, Vars.o)), Vars.p, Vars.o);
		
		BinaryRelation result = getFacets(basePath, br, Vars.p, effectiveConstraints);

		return result;
	}

	public BinaryRelation getFacets(P basePath, BinaryRelation facetRelation, Var pVar, Set<Expr> effectiveConstraints) {
		ExprTransform exprTransform = new ExprTransformViaPathMapper<>(mapper);
		
		
		
		Set<Element> elements = new LinkedHashSet<>();
		
		for(Expr expr : effectiveConstraints) {
			Set<P> paths = MainFacetedBenchmark2.getPathsMentioned(expr, pathAccessor.getPathClass());

			for(P p : paths) {
				BinaryRelation br = mapper.getOverallRelation(p);
				elements.addAll(ElementUtils.toElementList(br.getElement()));
			}
			
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
		Map<Var, Var> rename = VarUtils.createDistinctVarMap(vars, forbiddenVars, true, VarGeneratorBlacklist.create(forbiddenVars));
		//rename.put(facetRelation.getSourceVar(), s);
		
		s = rename.getOrDefault(s, s);
		
		List<Element> es = new ArrayList<>();
		for(Element e : elements) {
			Element x = ElementUtils.createRenamedElement(e, rename);
			es.add(x);
		}
		
		//boolean isReverse = pathAccessor.isReverse(path);
		//Triple t = QueryFragment.createTriple(isReverse, s, Vars.p, Vars.o);
		es.add(facetRelation.getElement());//ElementUtils.createElement(t));
		
		//BinaryRelation result = new BinaryRelation(ElementUtils.groupIfNeeded(es), Vars.p, Vars.o);
		BinaryRelation result = new BinaryRelation(ElementUtils.groupIfNeeded(es), pVar, facetRelation.getTargetVar());
		
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

		result.put(null, brr);
		
		return result;
	}
	
}


public class MainFacetedBenchmark2 {
	public static void init(Personality<RDFNode> p) {
		//p.add(Selection.class, new SimpleImplementation(SelectionImpl::new));
		p.add(SPath.class, new SimpleImplementation(SPathImpl::new));
		p.add(Dimension.class, new SimpleImplementation(DimensionImpl::new));
	}
	
	public static <P> Set<P> getPathsMentioned(Expr expr, Class<P> pathClass) {
		Set<P> result = Streams.stream(Traverser.forTree(ExprUtils::getSubExprs).depthFirstPreOrder(expr).iterator())
			.filter(e -> e instanceof ExprPath)
			.map(e -> ((ExprPath<?>)e).getPath())
			.filter(p -> !Objects.isNull(p) && pathClass.isAssignableFrom(p.getClass()))
			.map(p -> (P)p)
			.collect(Collectors.toSet());
		
		return result;
	}
	
	public static void main(String[] args) {
		JenaSystem.init();
		init(BuiltinPersonalities.model);
		
		FactoryWithModel<SPath> dimensionFactory = new FactoryWithModel<>(SPath.class);
		
		SPath root = dimensionFactory.get();		
		SPath somePath = root.get(RDF.type.getURI(), false);//.get(RDFS.seeAlso.toString(), true);

		PathAccessor<SPath> pathAccessor = new PathAccessorSPath();
		FacetedQueryGenerator<SPath> g = new FacetedQueryGenerator<>(pathAccessor);
		

		Expr tmp = new E_Equals(new ExprPath<>(somePath), NodeValue.makeNode(OWL.Class.asNode()));
//		Expr resolved = ExprTransformer.transform(exprTransform, tmp);


		g.getConstraints().add(tmp);

		Map<String, BinaryRelation> facets = g.getFacets(somePath.getParent(), false);
		
		System.out.println("FACETS: " + facets);
		
//		System.out.println("Path mentioned: " + getPathsMentioned(tmp, SPath.class));
//		
//		System.out.println("Relation: " + br);
//		System.out.println("Resolved: " + resolved);
		
		//CriteriaBuilder cb;
		//cb.
		//FacetedQuery q = new FacetedQuery();
		
		//constraintBlock.add(new E_Equals(mapper.getExpr(somePath), NodeValue.makeNode(OWL.Class.asNode())));
		
//		Set<Element> elements = pathToBgpMapper.getElements();
//		for(Element t : elements) {
//			System.out.println("Element: " + t);
//		}
		
//		SPath p1 = spathFactory.newInstance();
//		p1.setParent(root);
//		p1.setReverse(true);
//		p1.setPredicate(RDF.type);

		
		//System.out.println("Property = " + p1.getPredicate());
		RDFDataMgr.write(System.out, dimensionFactory.getModel(), RDFFormat.TURTLE);
	}
}

//PathToRelationMapper<SPath> mapper = new PathToRelationMapper<>(pathAccessor);
//DimensionConstraintBlock constraintBlock = new DimensionConstraintBlock();
//constraintBlock.getPaths().add(somePath);
//QueryFragment.toElement(somePath, new PathAccessorSPath(), elements, pathToNode, varGen)
//QueryFragment aq = QueryFragment.createForFacetCountRemainder(constraintBlock, Vars.s, Arrays.asList(), false);
//System.out.println(aq);
//ExprTransform exprTransform = new ExprTransformViaPathMapper<>(mapper);
//
//BinaryRelation br = mapper.getOverallRelation(somePath); //pathToBgpMapper.getOrCreate(somePath);
