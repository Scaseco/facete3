package org.aksw.facete.v3.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.jena.graph.GraphVar;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.PathAccessorRdf;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.aksw.jena_sparql_api.data_query.impl.PathToRelationMapper;
import org.aksw.jena_sparql_api.data_query.impl.QueryFragment;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;

import com.eccenca.access_control.triple_based.core.ElementTransformTripleRewrite;
import com.eccenca.access_control.triple_based.core.GenericLayer;


interface Resolver {
	Resolver resolve(P_Path0 stop);
	Collection<TernaryRelation> getContrib(boolean fwd);
}


class ResolverUnion
	implements Resolver
{
	protected Collection<? extends Resolver> resolvers;

	public ResolverUnion(Collection<? extends Resolver> resolvers) {
		super();
		this.resolvers = resolvers;
	}

	@Override
	public Resolver resolve(P_Path0 step) {
		Collection<Resolver> children = resolvers.stream().map(r -> r.resolve(step))
				.collect(Collectors.toList());
		
		Resolver result = new ResolverUnion(children);
		return result;
	}

	@Override
	public Collection<TernaryRelation> getContrib(boolean fwd) {
		List<TernaryRelation> result = new ArrayList<>();
		for(Resolver resolver : resolvers) {
			Collection<TernaryRelation> contribs = resolver.getContrib(fwd);
			result.addAll(contribs);
		}

		return result;
	}
}


/**
 * Resolution on a template yields 2 resolvers (if the step leads to a target):
 * - Another one on the template level
 * - One on the data level
 * 
 * These two resolvers are wrapped as a single ResolverUnion
 * 
 * @author raven
 *
 */
class ResolverTemplate
	implements Resolver
{
	protected PartitionedQuery1 query;
	protected Set<? extends RDFNode> starts;

	protected ResolverTemplate(PartitionedQuery1 query, Set<? extends RDFNode> starts) {
		this.query = query;
		this.starts = starts;
	}
	
	
	
	@Override
	public Resolver resolve(P_Path0 step) {
		ResolverUnion result = new ResolverUnion(Arrays.asList(
				resolveTemplate(step),
				resolveData(step)));
		
		return result;
	}
	

	protected Resolver resolveTemplate(P_Path0 step) {
		//Collection<RDFNode> starts = Collections.singleton(root); 
//			Property p = ResourceUtils.getProperty(step);
		Set<RDFNode> targets =
			starts.stream().flatMap(s ->
				ResourceUtils.listPropertyValues(s.asResource(), step).toList().stream())
			.collect(Collectors.toSet());
		
		
		return new ResolverTemplate(query, targets);
		
		//Element basePattern = query.getQueryPattern();

//		Set<Node> result = starts.stream().map(RDFNode::asNode).collect(Collectors.toSet());
	
	}
	
	protected Resolver resolveData(P_Path0 step) {
		return new ResolverData(query, Arrays.asList(step));
	}

	@Override
	public Collection<TernaryRelation> getContrib(boolean isFwd) {
		Collection<TernaryRelation> result = new ArrayList<>();
		
		Element basePattern = query.getQuery().getQueryPattern();
		Collection<Var> baseVars = PatternVars.vars(basePattern);
		
		// Find all outgoing predicates according to the template
		for(RDFNode rdfNode : starts) {
			
			Var var = (Var)rdfNode.asNode();
			UnaryRelation templateConcept = new Concept(basePattern, var);
			
			List<Statement> stmts = ResourceUtils.listProperties(rdfNode, isFwd).toList();
			for(Statement stmt : stmts) {
				Node s = stmt.getSubject().asNode();
				Node p = stmt.getPredicate().asNode();
				Node o = stmt.getObject().asNode();
				
				// Create a pattern ?s ?p ?o { <placeholder> BIND(?p = :const }
				// Then prepend the original pattern
//				TernaryRelation tr = new TernaryRelationImpl(
//						ElementUtils.groupIfNeeded(
//								ElementUtils.createElementTriple(Vars.s, p, Vars.o),
//								new ElementBind(Vars.p, NodeValue.makeNode(p))),
//						Vars.s, Vars.p, Vars.o);
				
				// The predicate of the triple view can be defined by the template or the pattern
				// In the first case, the predicate is a constant, otherwise its a variable
				
				TernaryRelation tr;
				if(p.isVariable()) {
					tr = new TernaryRelationImpl(basePattern,
						(Var)s, (Var)p, (Var)o);
				} else {
					// Allocate a fresh variable for 'p'
					Var freshP = VarGeneratorBlacklist.create(baseVars).next();
					
					NodeValue nvp = NodeValue.makeNode(p);
					tr = new TernaryRelationImpl(
							ElementUtils.groupIfNeeded(
									basePattern,
							new ElementBind(freshP, nvp)),
						(Var)s, freshP, (Var)o);					
				}
				
//				
//				TernaryRelation combined = tr
//						.prependOn((Var)s).with(templateConcept)
//						.toTernaryRelation();
						
				result.add(tr);
			}
			
			
			//rdfNode.asResource().listProperties().toList();
			
			
			//TernaryRelation tr = createRelation(isFwd, Vars.s, Vars.p, Vars.o);
			
			//Node p = NodeFactory.createURI("http://test");
			TernaryRelation tmp = new TernaryRelationImpl(
					//ElementUtils.groupIfNeeded(
							ElementUtils.createElement(QueryFragment.createTriple(!isFwd, Vars.s, Vars.p, Vars.o)),
							//new ElementBind(Vars.p, NodeValue.makeNode(p))),
					Vars.s, Vars.p, Vars.o);

			
			// I think the result of using joinOn is wrong as the lhs triple pattern gets remove
			// due to being a subject concept - however, the subject concept removal
			// must be suppressed if its variables are projected referred to
			TernaryRelation todebug =
					tmp
					.joinOn(tmp.getS()).with(templateConcept)
					.toTernaryRelation();

			
			TernaryRelation tr =
				tmp
				.prependOn(tmp.getS()).with(templateConcept)
				.toTernaryRelation();
			
			result.add(tr);

			// Create the data level contribution

			

			
//			TernaryRelation tr =
//					BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//					.joinOn(var).with(new Concept(basePattern, var))
//					.toTernaryRelation();

//			TernaryRelationImpl 
//			RDFNode from = ResourceUtils.getSource(stmt, isFwd).asNode();
//			RDFNode to = ResourceUtils.getSource(stmt, isFwd).asNode();

		
		}
		
		return result;
	}
	
}

class ResolverData
	implements Resolver
{
	//protected ResolverTemplate base;
	protected List<P_Path0> steps;

	protected PartitionedQuery1 query;
	//protected RDFNode start;

	
//	public ResolverData(ResolverTemplate base, List<P_Path0> steps) {
	public ResolverData(PartitionedQuery1 query, List<P_Path0> steps) {
		super();
		this.query = query;
		this.steps = steps;
		//this.base = base;
		//this.steps = steps;
	}

	@Override
	public Resolver resolve(P_Path0 step) {
		List<P_Path0> next = new ArrayList<>(steps);
		next.add(step);
		return new ResolverData(query, next);
	}

	public static TernaryRelation createRelation(boolean isFwd, Var s, Var p, Var o) {
		Triple t = QueryFragment.createTriple(!isFwd, s, p, o);
		TernaryRelation result = new TernaryRelationImpl(ElementUtils.createElement(t), s, p, o);
		return result;
	}
	
	@Override
	public Collection<TernaryRelation> getContrib(boolean isFwd) {
		
		Element basePattern = query.getQuery().getQueryPattern();
		Var baseVar = query.getPartitionVar();
		//Var baseVar = (Var)start.asNode();
		Concept baseConcept = new Concept(basePattern, baseVar);
		

		
		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
		PathToRelationMapper<SimplePath> mapper = new PathToRelationMapper<>(pathAccessor, "w");

		// We need to connect relations:
		// concept - path - triple pattern
		
		
		BinaryRelation pathRelation = mapper.getOverallRelation(new SimplePath(steps));
		
		BinaryRelation pathRelationWithConcept = pathRelation
			.prependOn(pathRelation.getSourceVar()).with(baseConcept)
			.toBinaryRelation();
		
		TernaryRelation tr = createRelation(isFwd, Vars.s, Vars.p, Vars.o);
		
		
		TernaryRelation result = tr
				.prependOn(tr.getS()).with(pathRelationWithConcept, pathRelationWithConcept.getTargetVar())
				.toTernaryRelation();

		return Arrays.asList(result);
	} 

}




//
//interface PathResolver<P extends PathResolver<P, S, T>, S, T> {
//	P parent();
//	P step(S step);
//	T value();
//}
//
//interface StepResolver<S, C> {
//	C resolveContrib(S step);
//}
//
//
//class ParentLikn
//
//class PathResolverSimple<P extends PathResolverSimple<P>>
//	implements PathResolver<P, P_Path0, BinaryRelation>
//{
//	
//	@Override
//	public P parent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public P step(P_Path0 step) {
//		BinaryRelationImpl.create(p)
//	}
//
//	@Override
//	public BinaryRelation value() {
//		
//		
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}

//
abstract class PathAccessorPath<P>
	implements PathAccessorRdf<P>
{
//	@Override
//	public P getParent(P path) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	protected abstract P_Path0 getLastStep(P path);

	@Override
	public BinaryRelation getReachingRelation(P path) {
		P_Path0 step = getLastStep(path);//Iterables.getLast(null);
		
		BinaryRelation result = BinaryRelationImpl.createFwd(Vars.s, step.getNode(), Vars.o);
		return result;
	}

	@Override
	public boolean isReverse(P path) {
		P_Path0 step = getLastStep(path); //Iterables.getLast(null);
		boolean result = !step.isForward();
		return result;
	}

	@Override
	public String getPredicate(P path) {
		P_Path0 step = Iterables.getLast(null);
		String result = step.getNode().getURI();
		return result;
	}

	@Override
	public Var getAlias(P path) {
		return null;
	}	
}
//
//
class PathAccessorSimplePath
	extends PathAccessorPath<SimplePath>
{
	@Override
	public SimplePath getParent(SimplePath path) {
		return path.parentPath();
	}

	@Override
	protected P_Path0 getLastStep(SimplePath path) {
		return path.lastStep();
	}
}




/**
 * State 
 * @author raven
 *
 */
class PartQueryNode {
	protected PartitionedQuery1 q;
	protected Var var;
}

/**
 * Notes on path resolutions:
 * 	- Aliased paths do not appear to make sense here:
 *    - One might think that aliases could be used to resolve properties in templates of
 *      partitioned queries such as ?s rdfs:label ?v1, ?v2.
 *      (E.g. consider a base table with multiple columns of alternative labels).
 *      If this is the way it is mapped, then we simply accept it here.
 *      There is no need to resolve rdfs:label to e.g. only ?v1 here - if this is desired, the place to
 *      where to "fix" this is in the partitioned query.
 *    - There is also does not appear to be a good reason / user case
 *      for aliases to affect the naming of variables:
 *      The output of the resolution in a virtual RDF graph according to the partitioned queries.
 *      In a virtual RDF graph, the naming of the variables are meaningless anyway,
 *      as the rewriting system on top just cares about subject, predicate and object positions
 *      but not how they are named.
 * 
 * 
 * @author raven
 *
 */
public class VirtualPartitionedQuery {
//
//	public static void rewrite(Collection<PartitionedQuery1> views, Iterable<Entry<P_Path0, String>> aliasedPath) {
//		// Rewrite a path over a collection of partitioned query views
//		
//		
//		
//		//return null;
//	}
//	
//	public void step(Collection<PartitionedQuery1> views, P_Path0 step, String alias) {
//		for(PartitionedQuery1 pq : views) {
//		
//		}
//	}
//
//	
//	// Note: The code below may not work with literals in the template due to
//	// jena not allowing literals to act as resources
//	// but actually its a pointless limitation for our purposes
//	public Resolver createResolver(PartitionedQuery1 pq, Iterable<? extends P_Path0> path) {
//		Node rootNode = pq.getPartitionVar();
//		
//		Query query = pq.getQuery();
//		Template template = query.getConstructTemplate();
//		GraphVar graphVar = new GraphVarImpl(GraphFactory.createDefaultGraph());
//		GraphUtil.add(graphVar, template.getTriples());
//		Model model = ModelFactory.createModelForGraph(graphVar);
//		
//		Resource root = model.getRDFNode(rootNode).asResource();
//		System.out.println(root.listProperties().toList());
//
//		Collection<RDFNode> starts = Collections.singleton(root); 
//		for(P_Path0 step : path) {
////			Property p = ResourceUtils.getProperty(step);
//			List<RDFNode> targets =
//				starts.stream().flatMap(s ->
//					ResourceUtils.listPropertyValues(s.asResource(), step).toList().stream())
//				.collect(Collectors.toList());
//			starts = targets;
//		}
//		
//		
//		//Element basePattern = query.getQueryPattern();
//
//		Set<Node> result = starts.stream().map(RDFNode::asNode).collect(Collectors.toSet());
//		return result;
//	}
//	
////	public static Set<Var> resolve(PartitionedQuery1 pq, Collection<Var> startVars, P_Path0 step) {
////		
////	}
//	
//	
//	public static Set<Var> resolve() {
//		//Relation baseRelation = RelationImpl.create(basePattern, PatternVars.vars(basePattern));
//
//		//FacetedQueryGenerator.createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
//
//		
//		List<TernaryRelation> trs;
//		for(RDFNode target : targets) {
//			// Generate the triple pattern (target, p, o)
//			Var var = (Var)target.asNode();
//			System.out.println(var);
//		
//			BinaryRelation br =
//				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//				.joinOn(var).with(new Concept(basePattern, var))
//				.toBinaryRelation();
//			
//		}		
//	}
//	
//	
	
	
	public static Resolver createResolver(PartitionedQuery1 pq) {
		RDFNode node = toRdfModel(pq);
		Resolver result = new ResolverTemplate(pq, Collections.singleton(node));
		return result;
	}

	
	public static RDFNode toRdfModel(PartitionedQuery1 pq) {
		Node rootNode = pq.getPartitionVar();
		
		Query query = pq.getQuery();
		Template template = query.getConstructTemplate();
		GraphVar graphVar = new GraphVarImpl(GraphFactory.createDefaultGraph());
		GraphUtil.add(graphVar, template.getTriples());
		Model model = ModelFactory.createModelForGraph(graphVar);
		
		Resource root = model.getRDFNode(rootNode).asResource();

		return root;
	}
	
//	public void step(SimplePath basePath, PartitionedQuery1 pq, P_Path0 step, boolean isFwd, String alias) {
//		System.out.println(root.listProperties().toList());
//		
//		Property p = ResourceUtils.getProperty(step);
//		List<RDFNode> targets = ResourceUtils.listPropertyValues(root, step).toList();
//		
//		Element basePattern = query.getQueryPattern();
//		//Relation baseRelation = RelationImpl.create(basePattern, PatternVars.vars(basePattern));
//
//		//FacetedQueryGenerator.createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
//
//		
//		List<TernaryRelation> trs;
//		for(RDFNode target : targets) {
//			// Generate the triple pattern (target, p, o)
//			Var var = (Var)target.asNode();
//			System.out.println(var);
//		
//			BinaryRelation br =
//				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//				.joinOn(var).with(new Concept(basePattern, var))
//				.toBinaryRelation();
//			
//		}
//		
////		// Resolve the path to a 
////		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
////		PathToRelationMapper<SimplePath> mapper = new PathToRelationMapper<>(pathAccessor, "w");
////
////		basePath.
////		mapper.getOverallRelation(path);
//		
////		BinaryRelation br =
////				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
////				.joinOn(var).with(new Concept(basePattern, var))
////				.toBinaryRelation();
//		
//		
//		
//		System.out.println(ResourceUtils.listPropertyValues(root, step).toList());
//	}

	public static TernaryRelation unionTernary(Collection<? extends TernaryRelation> items) {
		Relation tmp = union(items, Arrays.asList(Vars.s, Vars.p, Vars.o));
		TernaryRelation result = tmp.toTernaryRelation();
		return result;
	}

	
	public static Relation union(Collection<? extends Relation> items, List<Var> proj) {
		List<Element> elements = items.stream()
				.map(e -> RelationUtils.rename(e, proj))
				.map(Relation::getElement)
				.collect(Collectors.toList());
		
		Element e = ElementUtils.unionIfNeeded(elements);

		Relation result = new RelationImpl(e, proj);
		return result;
	}
	
	
//	public static Query rewrite(Resolver resolver, boolean isFwd, Query query) {
//		Collection<TernaryRelation> views = resolver.getContrib(true);
//
//		TernaryRelation tr = unionTernary(views);
////		System.out.println(tr);
//		
//		GenericLayer layer = GenericLayer.create(tr);
//		
//		Query raw = ElementTransformTripleRewrite.transform(query, layer, true);
//		Query result = DataQueryImpl.rewrite(raw, DataQueryImpl.createDefaultRewriter()::rewrite);
//
//		if(false) {
//			System.out.println("Views:");
//			for(TernaryRelation view : views) {
//				System.out.println(view);
//			}
//		}
//
//		return result;
//	}
//	
	public static Resolver createResolver(Var viewVar, Query view) {
		PartitionedQuery1 pq = new PartitionedQuery1(view, viewVar);
		Resolver result = createResolver(pq);

		return result;
	}
	
	public static Query rewrite(Collection<TernaryRelation> views, Query query) {
//		Resolver resolver = createResolver(view, viewVar);
//		Query result = rewrite(resolver, true, query);
		TernaryRelation tr = unionTernary(views);
//		System.out.println(tr);
		
		GenericLayer layer = GenericLayer.create(tr);
		
		Query raw = ElementTransformTripleRewrite.transform(query, layer, true);
		Query result = DataQueryImpl.rewrite(raw, DataQueryImpl.createDefaultRewriter()::rewrite);

		return result;
	}

	public static void main(String[] args) {
		
		Query view = QueryFactory.create("CONSTRUCT { ?p <http://facetCount> ?c } { { SELECT ?p (COUNT(?o) AS ?c) { ?s ?p ?o } GROUP BY ?p } }");
		
		Resolver resolver = createResolver(Vars.p, view);
		
		Query example1 = rewrite(
				resolver
					.getContrib(true),
				QueryFactory.create("SELECT ?x ?y ?z { ?x ?y ?z }"));
		System.out.println("Example 1\n" + example1);

		Query example2 = rewrite(
				resolver
					.getContrib(true),
				QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
		System.out.println("Example 2\n" + example2);

		Query example3 = rewrite(
				resolver
					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")))	
					.getContrib(true),
				QueryFactory.create("SELECT ?x ?y ?z { ?x ?y ?z }"));
		System.out.println("Example 3\n" + example3);

		Query example4 = rewrite(
				resolver
					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")))	
					.getContrib(true),
				QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
		System.out.println("Example 4\n" + example4);
	}
	
	//processor.step(pq, new P_Link(NodeFactory.createURI("http://facetCount")), true, "a");
	
	
	//VirtualPartitionedQuery processor = new VirtualPartitionedQuery();
	


//	Query query = QueryFactory.create("CONSTRUCT { ?city <http://hasMayor> ?mayor . ?mayor <http://hasParty> ?party } { ?city <http://hasMayor> ?mayor . ?mayor <http://hasParty> ?party }");
//	PartitionedQuery1 pq = new PartitionedQuery1(query, Var.alloc("city"));
//	Resolver resolver = createResolver(pq);
//	resolver = resolver.resolve(new P_Link(NodeFactory.createURI("http://hasMayor")));


}

