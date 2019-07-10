package org.aksw.facete.v3.api.path;

import org.apache.jena.sparql.core.Var;

public interface VarRefEntry
	extends VarRef
{
	RelationletEntry<?> getEntry();
	Var getVar();
}