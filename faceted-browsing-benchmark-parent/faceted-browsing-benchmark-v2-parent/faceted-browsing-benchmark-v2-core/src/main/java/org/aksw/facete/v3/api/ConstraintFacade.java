package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.NodeHolder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Range;

/**
 * In general, there are anonymous and named constraints.
 * Named constraints can be
 * 
 * 
 * @author raven
 *
 */
public interface ConstraintFacade<B> {
	Collection<FacetConstraint> list();

	Collection<HLFacetConstraint<? extends ConstraintFacade<B>>> listHl(); 
	
	default Stream<FacetConstraint> stream() {
		return list().stream();
	}
	
	/** Add an anonymous equal constraint */
	HLFacetConstraint<ConstraintFacade<B>> eq(Node node);
	ConstraintFacade<B> exists();
	ConstraintFacade<B> gt(Node node);
	ConstraintFacade<B> neq(Node node);

	
	ConstraintFacade<B> range(Range<NodeHolder> range);

	/**
	 * Return the expr that denotes the ConstraintFacade's underlying
	 * FacetNode or FacetMultiNode.
	 * @return
	 */
	Expr thisAsExpr();
	
	boolean hasExpr(Expr expr);
	ConstraintFacade<B> addExpr(Expr expr);
	boolean removeExpr(Expr expr);
	

	default boolean toggle(Expr expr) {
		boolean alreadySet = hasExpr(expr);
		if(!alreadySet) {
			addExpr(expr);
		} else {
			removeExpr(expr);
		}
		
		return !alreadySet;
	}
	

	
	default HLFacetConstraint<ConstraintFacade<B>> eqIri(String iriStr) {
		return eq(NodeFactory.createURI(iriStr));
	}

	default HLFacetConstraint<ConstraintFacade<B>> eq(String stringLiteral) {
		return eq(NodeFactory.createLiteral(stringLiteral));
	}
	
	default HLFacetConstraint<ConstraintFacade<B>> eq(RDFNode rdfNode) {
		return eq(rdfNode.asNode());
	}

//	default find(Function<? super Expr, ? extends Expr> expr) {
//		
//	}
//	
//	default Stream<HLFacetConstraint> find(Class<?> exprType, Node ... nodes) {
//		Stream<HLFacetConstraint> result = listHl().stream()
//			.filter(c -> exprType.isAssignableFrom(c.expr().getClass())
//					;
//			//.filter(x -> true);
//		return result;
//	}
	
//	default ConstraintFacade<B> exists(RDFNode rdfNode) {
//		return exists(rdfNode.asNode());
//	}

	/** End constraint building and return the parent object */
	B end();
}
