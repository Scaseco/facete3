package org.aksw.facete.v3.api.path;

import org.aksw.jena_sparql_api.concepts.Relation;


/**
 * The more general perspective on path is, that they are triple-like:
 * 
 * 
 * 
 * @author raven
 *
 */
public class Pathlet {
	Containlet model;
	Nodelet start;
	Nodelet target;

	
	public void createInModel(Containlet model) {
		
	}
	
	/**
	 * 
	 * @param optional
	 * @param relation Either a Binary- or TernaryRelation instance
	 * @param alias
	 * @return
	 */
	Pathlet join(boolean optional, Relation relation, String alias) {
		return null;
	}
}
