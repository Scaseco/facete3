package org.aksw.jena_sparql_api.pathlet;

import org.aksw.facete.v3.api.path.Step;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;

public abstract class PathBuilder {
	public Path optional() {
		return optional("", null);
	}

	public Path optional(Object key) {
		return optional(key, null);
	}

	public Path optional(Object key, String alias) {
		return appendStep(new Step("optional", key, null));
	}
	
	public Path fwd(String str) {
		return step(true, str, null);
	}

	public Path fwd(String str, String alias) {
		return step(true, str, alias);
	}

	public Path step(boolean isFwd, String pStr, String alias) {
		Node p = NodeFactory.createURI(pStr);
		P_Path0 path = PathUtils.createStep(p, isFwd);

		return step(path, alias);
	}

	public Path fwd(Resource p) {
		return fwd(p, null);
	}
	
	public Path step(P_Path0 p, String alias) {
		return appendStep(new Step("br", p, alias));
	}

	public Path step(BinaryRelation br, String alias) {
		return appendStep(new Step("br", br, alias));
	}

	public Path fwd(Resource p, String alias) {
//		BinaryRelation br = RelationUtils.createRelation(p.asNode(), false);
		return step(PathUtils.createStep(p.asNode(), true), alias);
	}

	public abstract Path appendStep(Step step);
}