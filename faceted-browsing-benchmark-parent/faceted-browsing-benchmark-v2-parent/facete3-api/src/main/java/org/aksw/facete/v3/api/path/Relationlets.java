package org.aksw.facete.v3.api.path;

import org.apache.jena.sparql.syntax.Element;

public class Relationlets {
	public static Relationlet from(Element e) {
		return new RelationletElementImpl(e);
	}
}