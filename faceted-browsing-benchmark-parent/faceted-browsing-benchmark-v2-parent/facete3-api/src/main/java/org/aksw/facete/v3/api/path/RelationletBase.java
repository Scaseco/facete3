package org.aksw.facete.v3.api.path;

import java.util.Collection;

import org.apache.jena.sparql.core.Var;

public abstract class RelationletBase
	implements Relationlet
{
	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		Collection<Var> fixedVars = getFixedVars();
		if(onOrOff) {
			fixedVars.add(var);
		} else {
			fixedVars.remove(var);
		}

		return this;
	}
}
