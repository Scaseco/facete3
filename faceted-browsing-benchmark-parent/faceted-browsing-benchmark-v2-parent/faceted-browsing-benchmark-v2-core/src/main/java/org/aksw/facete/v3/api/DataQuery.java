package org.aksw.facete.v3.api;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.facete.v3.impl.NodePath;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeUtils;

import io.reactivex.Flowable;
import io.reactivex.Single;
import jersey.repackaged.com.google.common.collect.Iterables;


interface MultiNode {
	
}



// Actually, this is pretty much a resource

/**
 * Query builder for retrieval batch retrieval of related *optional* information for each entity of an underlying
 * relation.
 * 
 * Hence, limit and offset apply to the base relation.
 * 
 * 
 * @author raven
 *
 */
public interface DataQuery<T extends RDFNode> {
	// For every predicate, list how many root root resources there are having this predicate
	//getPredicatesAndRootCount();
	
	DataQuery<T> peek(Consumer<? super DataQuery<T>> consumer);
	NodePath get(String attrName);
	
	
	Single<Model> execConstruct();
	
	UnaryRelation fetchPredicates();
	
	DataNode getRoot();
	
	DataMultiNode add(Property property);
	
	
	// Return the same data query with intersection on the given concept
	DataQuery<T> filter(UnaryRelation concept);

	default DataQuery<T> filter(String exprStr) {
		Expr expr = ExprUtils.parse(exprStr);
		return filter(expr);
	}

	public static UnaryRelation toUnaryFiler(Expr expr) {
		Set<Var> vars = ExprVars.getVarsMentioned(expr);
		if(vars.size() != 1) {
			throw new IllegalArgumentException("Provided expression must contain exactly 1 variable");
		}
		
		Var var = Iterables.getFirst(vars, null);
		UnaryRelation result = new Concept(new ElementFilter(expr), var);		
		return result;
	}
	
//	default DataQuery<T> filter(Expr expr) {
//		UnaryRelation ur = toUnaryFiler(expr);
//		
//		return filter(ur);
//	}

	DataQuery<T> filter(Expr expr);

	// Filter injection without renaming variables
	DataQuery<T> filterDirect(Element element);

	DataQuery<T> connection(SparqlQueryConnection connection);
	SparqlQueryConnection connection();
	
	
	default DataQuery<T> only(Iterable<Node> nodes) {
		Expr e = new E_OneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes));
		return filter(new Concept(new ElementFilter(e), Vars.s));				
	}

	default DataQuery<T> only(Node ... nodes) {
		return only(Arrays.asList(nodes));
	}

	default DataQuery<T> only(RDFNode ... rdfNodes) {
		return only(Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
	}

	default DataQuery<T> only(String ... iris) {
		return only(NodeUtils.convertToNodes(Arrays.asList(iris)));
	}

	
	
	default DataQuery<T> exclude(Iterable<Node> nodes) {
		Expr e = new E_NotOneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes));
		return filter(new Concept(new ElementFilter(e), Vars.s));				
	}

	default DataQuery<T> exclude(Node ... nodes) {
		return exclude(Arrays.asList(nodes));
	}

	default DataQuery<T> exclude(RDFNode ... rdfNodes) {
		return exclude(Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
	}

	default DataQuery<T> exclude(String ... iris) {
		return exclude(NodeUtils.convertToNodes(Arrays.asList(iris)));
	}
	
	DataQuery<T> limit(Long limit);

	default DataQuery<T> limit(Integer limit) {
		return limit(limit == null ? null : limit.longValue());
	}

	DataQuery<T> offset(Long offset);

	default DataQuery<T> offset(Integer offset) {
		return offset(offset == null ? null : offset.longValue());
	}

	DataQuery<T> sample(boolean onOrOff);
	
	default DataQuery<T> sample() {
		return sample(true);
	}
	
	boolean isSampled();
	
	
	DataQuery<T> ordered(boolean onOrOff);
	
	default DataQuery<T> ordered() {
		return ordered(true);
	}
	
	boolean isOrdered();


	DataQuery<T> randomOrder(boolean onOrOff);
	
	default DataQuery<T> randomOrder() {
		return randomOrder(true);
	}
	
	boolean isRandomOrder();
	
	
	
	/**
	 * Return a SPARQL construct query together with the designated root variable
	 * 
	 * TODO Do we need to revise the return value to allow multiple root variables? Maybe yield a Relation instance?
	 * @return
	 */
	Entry<Node, Query> toConstructQuery();

	
	Relation baseRelation();

	Flowable<T> exec();
}
