package org.aksw.jena_sparql_api.relationlet;

import org.apache.jena.sparql.syntax.Element;

public interface RelationletElement
	extends Relationlet
{
	Element getElement();
}