package org.aksw.facete.v3.api.path;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.apache.jena.rdf.model.Resource;

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
	
	public Path fwd(Resource p) {
		return fwd(p, null);
	}

	public Path fwd(Resource p, String alias) {
		BinaryRelation br = RelationUtils.createRelation(p.asNode(), false);
		return appendStep(new Step("br", br, alias));
		
	}

	public abstract Path appendStep(Step step);
}