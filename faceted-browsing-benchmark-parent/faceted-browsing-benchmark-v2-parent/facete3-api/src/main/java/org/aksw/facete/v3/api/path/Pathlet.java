package org.aksw.facete.v3.api.path;

import org.apache.jena.sparql.core.Var;

public interface Pathlet
	extends Relationlet
{
	Var getSrcVar();
	Var getTgtVar();
	
}