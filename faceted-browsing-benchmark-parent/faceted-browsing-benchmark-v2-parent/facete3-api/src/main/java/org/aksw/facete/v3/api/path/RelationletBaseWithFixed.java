package org.aksw.facete.v3.api.path;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

// Probably we need to distinguish between simple relationlets with 'constant' vars
// and those with dynamic vars, which means, that variable referred to by a varref can change 
public abstract class RelationletBaseWithFixed
	extends RelationletBase
{
	protected Set<Var> fixedVars = new LinkedHashSet<>();
	protected Set<Var> exposedVars = new LinkedHashSet<>();

	@Override
	public Relationlet getMember(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Var getInternalVar(Var var) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Var> getExposedVars() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<Var> getFixedVars() {
		return fixedVars;
	}



//	@Override
//	public Set<Var> getFixedVars() {
//		return fixedVars;
//	}
//
////	@Override
////	public Set<Var> getVarsMentioned() {
////		// TODO Auto-generated method stub
////		return null;
////	}
//
//	@Override
//	public Relationlet setVarFixed(Var var, boolean onOrOff) {
//		if(onOrOff) {
//			fixedVars.add(var);
//		} else {
//			fixedVars.remove(var);
//		}
//
//		return this;
//	}

//	@Override
//	public Element getElement() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public RelationletNested materialize() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	

//	@Override
//	public Relationlet setVarFixed(Var var, boolean onOrOff) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
}