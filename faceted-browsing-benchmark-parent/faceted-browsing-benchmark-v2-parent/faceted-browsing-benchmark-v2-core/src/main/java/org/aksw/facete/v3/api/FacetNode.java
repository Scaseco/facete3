package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;

import com.google.common.annotations.Beta;


/**
 * An object backed by the set of resources at a certain (possibly empty) path of properties.
 * 
 * 
 * @author Claus Stadler, Jul 23, 2018
 *
 */
public interface FacetNode
	extends NodeNavigation<FacetNode, FacetDirNode, FacetMultiNode>, Castable
{

	FacetedQuery query();

	/**
	 * Change the root of this FacetNode's faceted query to this node.
	 * All transitive parents of this node are turned to children.
	 * 
	 * Implementations of this operation should leave all FacetNodes intact.
	 * FacetDirNodes may become invalidated by this operation under certain conditions.
	 * 
	 * 
	 * 
	 * 
	 * TODO Below is probably outdated by now - check
	 * It is not totally clear to me, what exact changes this method should do, but the corner stones are:
	 * - There should be no changes to the id of all nodes in the rdf model corresponding to
	 *   any of FacetNode, FacetDirNode and Constraint
	 *   -> Clarify, whether the nodes used for the map structures on BgpMultiNode can remain the same
	 *   
	 *   Ideally, changing the root back should give exactly the same query as before.
	 * 
	 * - The changes are
	 *   - Set the query()'s root to this node
	 *   - Update the directions of the transitions accordingly
	 *   - As this node becomes the root, its underlying BgpNode's parent must become null in the process
	 * 
	 * 
	 * @return
	 */
	@Beta
	FacetNode chRoot();
	
	@Beta
	FacetNode chFocus();
	
	
	FacetNode as(String varName);
	FacetNode as(Var var);
	Var alias();


	FacetNode parent();

	BinaryRelation getReachingRelation();
	
	FacetNode root();
	
	/** Get the set of simple constraints affecting this facet.
	 * Simple constraints are expressions making use of only a single variable.
	 * The set of constraints is treated as a disjunction */
	//Set<Expr> getConstraints();

	/**
	 * List all
	 * 
	 * @return
	 */
//	Set<FacetConstraint> constraints();

	ConstraintFacade<? extends FacetNode> constraints();
	
	//Concept toConcept();
	
	// TODO Some API to get the values of this node by excluding all constraints
	DataQuery<RDFNode> availableValues();
	DataQuery<RDFNode> remainingValues();
}

