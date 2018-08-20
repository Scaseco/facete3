package org.hobbit.benchmark.faceted_browsing.v2.domain;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.rdf.model.Resource;

/**
 * A dimension is an intensional description of a set of values.
 * 
 * Usually, it abstracts a sequence of (predicate, direction) pairs,
 * where direction can be 'forward' or 'reverse'.
 * However, instead of only predicates, any binary SPARQL relation can be used.
 * 
 * 
 * @author Claus Stadler, May 30, 2018
 *
 */
public interface Dimension
	extends Resource, SPath
{
	/** A unique immutable variable representing this dimension
	 * so it can participate in SPARQL constraint expressions.
	 * Query generation will substitute this id for the alias if it is not null */
	// Not needed - .asNode() does that
	//Var getUniqueId();
	
//	void registerVirtualPredicate(Node virtualPredicate, Dimension dimension);

	/**
	 * The parent dimension
	 * @return
	 */
	Dimension getParent();
	
	/** The concept for the set of values at this dimension */
	Concept getValueConcept();
	
	/**
	 * A map for associating facets with corresponding binary relations of
	 * predicate-value pairs.
	 * The 'null' entry holds the binary relation of all predicates
	 * unaffected by any constraints.
	 * 
	 * @return
	 */
	Map<String, BinaryRelation> getOutgoingFacets();
	Map<String, BinaryRelation> getIncomingFacets();
	
	/** The concept for the set of outgoing predicates
	 * may be restricted to a subset of the value's predicates or may introduce virtual predicates */
	// TODO What if we wanted to restrict the set of values to those that have properties within the outgoing predicates set?
	//Concept getOutgoingPredicatesConcept();

	/** The concept for the set of incoming predicates
	 *  may be restricted to a subset of the value's predicates or may introduce virtual predicates */
	//Concept getIncomingPredicatesConcept();

	/** Obtain the primary sub-dimension by navigating along the predicate in the given direction.
	 *  This does not register the dimension as a child.
	 */
	// TODO How to design the api to add multiple sub-dimensions on the same property?
	// This is somewhat similar to listing all of a resource's statements with a given property
	Dimension getPrimarySubDimension(String predicate, boolean isReverse);

	/**
	 * Register a sub dimension created with this dimension.
	 * 
	 * @param subDimension
	 */
//	void registerSubDimension(Dimension subDimension);
//	
//	void unregisterSubDimension(Dimension subDimension);
//	
//	Collection<Dimension> getRegistredDimensions();
	
	
	/** Obtain a binary (SPARQL) relation, whose target are the values of this dimension */
	BinaryRelation getReachingBinaryRelation();
	boolean isReverse();
	
	/**
	 * Instanciate the dimension under a new virtual predicate.
	 * The instance can be independently constrained, which allows creating
	 * conjunctive constraints over multi-valued predicates.
	 * 
	 * For example, consider a set of resources of which some are typed with combinations
	 * of Student and Tutor.
	 * The concept StudentTutor can be created by
	 * constraining rdf:type = Student and then creating an
	 * instance of rdf:type with rdf:type_inst = Tutor.
	 * 
	 * The instance has the same parent as the dimension from which it is created.
	 */
	//Dimension instanciate(Node predicate);
	
	
	/** Probably a dimension should only be allowed to have 
	 *  a single alias (corresponds to a sparql variable) */
//	void setAlias(Var var);
//	Var getAlias();

	
}
