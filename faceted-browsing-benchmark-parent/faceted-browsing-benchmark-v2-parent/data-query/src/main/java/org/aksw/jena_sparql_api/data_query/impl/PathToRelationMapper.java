package org.aksw.jena_sparql_api.data_query.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.data_query.api.PathAccessor;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.graph.SuccessorsFunction;
import org.apache.jena.ext.com.google.common.graph.Traverser;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;

import com.github.davidmoten.guavamini.Lists;


/**
 * Stateful mapper that creates SPARQL BinaryRelation objects from paths,
 * thereby ensuring consistent variable naming
 * 
 * The mapper does not track changes - such as modifying a path's alias
 * 
 * 
 * 
 * @author Claus Stadler, May 30, 2018
 *
 * @param <P>
 */
public class PathToRelationMapper<P> {
	
	//protected Set<Triple> triples;
	protected PathAccessor<P> pathAccessor;
	//protected Set<Element> elements;
	protected Map<P, BinaryRelation> map;
	protected Set<Var> forbiddenVars;
	protected Generator<Var> varGen;
	
	
	public static <P> NodeTransform createNodeTransformSubstitutePathReferences(
			Function<? super Node, ? extends P> tryMapToPath,
			Function<? super P, ? extends Node> mapToNode) {
		return n -> Optional.ofNullable(
				tryMapToPath.apply(n))
				.map(x -> (Node)mapToNode.apply(x))
				.orElse(n);
	}
	
//	public void setRootVar(Var var) {
//		
//	}
	
//	public static <P> NodeTransform createNodeTransformSubstitutePathReferences(PathAccessor<P> pathAccessor) {
//		PathToRelationMapper<P> mapper = new PathToRelationMapper<>(pathAccessor);
//		
//		NodeTransform result = createNodeTransformSubstitutePathReferences(
//				pathAccessor::tryMapToPath,
//				mapper::getNode);
//
//		return result;
//	}

	public PathToRelationMapper(PathAccessor<P> pathAccessor) {
		this(pathAccessor, null);
	}
	
	public PathToRelationMapper(PathAccessor<P> pathAccessor, String baseName) {
		// Note: We cannot use an identity hash map if we use RDF-backed resources,
		// as multiple of these stateless vies may created for the same backing node
		this(pathAccessor,  new HashMap<>(), new LinkedHashSet<>(), VarGeneratorImpl2.create(baseName));
	}
	
	public PathAccessor<P> getPathAccessor() {
		return pathAccessor;
	}
	
//	public Expr getExpr(P path) {
//		BinaryRelation br = getOrCreate(path);
//		Var var = br.getTargetVar();
//		Expr result = new ExprVar(var);
//		return result;
//	}

	public Node getNode(P path) {
		BinaryRelation br = getOrCreate(path);
		Var result = br.getTargetVar();
//		Expr result = new ExprVar(var);
		return result;
	}

	public PathToRelationMapper(
			PathAccessor<P> pathAccessor,
			Map<P, BinaryRelation> map,
			Set<Var> forbiddenVars,
			Generator<Var> varGen) {
		super();
		this.pathAccessor = pathAccessor;
		//this.elements = elements;
		this.map = map;
		this.forbiddenVars = forbiddenVars;
		this.varGen = varGen;
	}

//	public Var getAlias(P path) {
//		Var result = map.get(path);
//		return result;
//	}
	
	public BinaryRelation getOverallRelation(P path) {
		
		// Initialize the segments in {@link map} for the given pat
		getOrCreate(path);
		
		
		SuccessorsFunction<P> fn = p ->
			Optional.ofNullable(pathAccessor.getParent(p)).map(Collections::singleton).orElse(Collections.emptySet());
		
		List<P> segments = Lists.newArrayList(Traverser.forTree(fn).depthFirstPostOrder(path));
		//Collections.reverse(segments);

		List<BinaryRelation> brs = segments.stream()
			.map(map::get)
			// Filter out the root segment which corresponds to an empty path
			//.filter(x -> !BinaryRelation.isEmpty(x))
			.collect(Collectors.toList());
		
		List<Element> elts = brs.stream()
				.filter(x -> !x.isEmpty())
				.map(BinaryRelation::getElement)
				.collect(Collectors.toList());
		
		Element elt = ElementUtils.groupIfNeeded(elts);
		Var s = Iterables.getFirst(brs, null).getSourceVar();
		Var o = Iterables.getLast(brs, null).getTargetVar();
		
		
		BinaryRelation result = new BinaryRelationImpl(elt, s, o);
		
		//TreeUtils.inOrderSearch(path, path -> Streams.stream()));
		
		return result;
	}
	
	public BinaryRelation getOrCreate(P path) {
		BinaryRelation result = QueryFragment.toElement(path, pathAccessor, map, forbiddenVars, varGen);
		return result;
	}
	
	

//	public Set<Element> getElements() {
//		return elements;
//	}	
}
