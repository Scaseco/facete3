package org.aksw.facete.v3.api;

import org.apache.jena.rdf.model.RDFNode;

public interface FacetQueryBuilder<T extends RDFNode> {
	FacetDirNode parent();
	FacetQueryBuilder<T> withCounts(boolean onOrOff);
	
	
	default FacetQueryBuilder<T> withCounts() {
		return withCounts(true);
	}

	<X extends RDFNode> FacetValueQueryBuilder<X> itemsAs(Class<X> itemClazz);

	DataQuery2<T> query2();
}
