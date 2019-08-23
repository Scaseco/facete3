package org.aksw.jena_sparql_api.relationlet;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public abstract class RelationletForwarding
	implements Relationlet
{
	protected abstract Relationlet getRelationlet();
	

	@Override
	public Relationlet getMember(String alias) {
		return getRelationlet().getMember(alias);
	}

	@Override
	public Var getInternalVar(Var var) {
		return getRelationlet().getInternalVar(var);
	}

	@Override
	public Collection<Var> getExposedVars() {
		return getRelationlet().getExposedVars();
	}

	@Override
	public Set<Var> getVarsMentioned() {
		return getRelationlet().getVarsMentioned();
	}

	@Override
	public Set<Var> getFixedVars() {
		return getRelationlet().getFixedVars();
	}

	@Override
	public Relationlet setFixedVar(Var var, boolean onOrOff) {
		return getRelationlet().setFixedVar(var, onOrOff);
	}

//	@Override
//	public Element getElement() {
//		return getRelationlet().getElement();
//	}

	@Override
	public RelationletNestedImpl materialize() {
		RelationletNestedImpl result = getRelationlet().materialize();
		return result;
	}

}