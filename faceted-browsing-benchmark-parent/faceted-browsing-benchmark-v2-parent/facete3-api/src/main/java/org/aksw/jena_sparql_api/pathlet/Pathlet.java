package org.aksw.jena_sparql_api.pathlet;

import org.aksw.jena_sparql_api.relationlet.Relationlet;
import org.apache.jena.sparql.core.Var;

public interface Pathlet
	extends Relationlet
{
	Var getSrcVar();
	Var getTgtVar();
	
}