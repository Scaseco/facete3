package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.Set;

import org.aksw.facete.v3.api.path.NestedVarMap;
import org.apache.jena.sparql.core.Var;

/**
 * Probably Relationlet should become the new Relation class
 * 
 * The question is how Relation's distinguished vars translate to this class -
 * is it exposedVars? - not really; exposedVars is a Set whereas distinguished vars is a list
 *  
 * @author raven
 *
 */
public interface Relationlet {
	Relationlet getMember(String alias);
	Var getInternalVar(Var var);

	Collection<Var> getExposedVars();
	Set<Var> getVarsMentioned();
	
	
	default boolean isFixed(Var var) {
		Set<Var> fixedVars = getFixedVars();
		boolean result = fixedVars.contains(var);
		return result;
	}
	
	default Relationlet fix(Var var) {
		return setFixedVar(var, true);
	}

	/**
	 * Adds all variables <b>currently</b> returned by getVarsMentioned() to the set of fixed vars.
	 * Does not mark vars that become available in the future as fixed.
	 * 
	 * @return
	 */
	default Relationlet fixAllVars() {
		Set<Var> vars = getVarsMentioned();
		Relationlet result = fixAllVars(vars);
		return result;
	}
	
	default Relationlet fixAllVars(Iterable<Var> vars) {
		for(Var var : vars) {
			setFixedVar(var, true);
		//return setVarFixed(var, true);
		}
		return this;
	}

	Set<Var> getFixedVars();
	Relationlet setFixedVar(Var var, boolean onOrOff);
	
//	Element getElement();
	
	NestedVarMap getNestedVarMap();
	RelationletNestedImpl materialize();
}