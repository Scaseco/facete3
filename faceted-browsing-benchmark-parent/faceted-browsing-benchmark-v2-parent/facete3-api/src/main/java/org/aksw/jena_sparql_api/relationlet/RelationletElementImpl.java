package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationletElementImpl
	//implements Relationlet
	extends RelationletElementBase
{
	protected Element el;
	protected Set<Var> fixedVars;

	public RelationletElementImpl(Element el) {
		this(el, new LinkedHashSet<>());
	}

	public RelationletElementImpl(Element el, Set<Var> fixedVars) {
		super();
		this.el = el;
		this.fixedVars = fixedVars;
	}

	@Override
	public Relationlet getMember(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Var getInternalVar(Var var) {
		return null;
	}

	@Override
	public Collection<Var> getExposedVars() {
		return null;
	}

	@Override
	public Element getElement() {
		return el;
	}

	@Override
	public Set<Var> getFixedVars() {
		return fixedVars;
	}

	@Override
	public Relationlet setFixedVar(Var var, boolean onOrOff) {
		boolean tmp = onOrOff
			? fixedVars.add(var)
			: fixedVars.remove(var);
			
		
		return this;
		//return result;
	}

	@Override
	public String toString() {
		return getElement() + " (fixed " + getFixedVars() + ")";
	}


//	@Override
//	public RelationletNested materialize() {
//		
//		return this;
//	}
}