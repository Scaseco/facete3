package org.aksw.facete.v3.api.path;

/**
 * A pathlet is a relationlet with designated source and target variables plus
 * operators for concatenation of pathlets
 * 
 * @author raven
 *
 */
interface PathletContainer
	extends Pathlet
{
	/**
	 * Add a left-join
	 * 
	 * { // ElementGroup
	 *   lhs
	 *   OPTIONAL {
	 *     rhs
	 *   }
	 * }
	 * 
	 * @return
	 */
	Pathlet optional(Pathlet rhs);
	
	// get or create an optional block with the given label
	Pathlet optional(String label);
	
	// get or create an optional block with a null label
    default Pathlet optional() {
    	return optional((String)null);
    }
	
    static Pathlet as(String alias) {
	    return null;
    }
}