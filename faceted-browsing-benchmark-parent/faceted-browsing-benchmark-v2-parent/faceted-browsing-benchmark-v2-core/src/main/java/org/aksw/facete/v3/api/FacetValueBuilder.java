package org.aksw.facete.v3.api;

import org.apache.jena.rdf.model.RDFNode;

/**
 * Idea for API improvement to give more control over how to construct the query for facet values
 * 
 * Status quo: facetDirNode.facetValueCounts().exec()
 * Goal of this class: facetDirNode.facetValues().withCounts().includeAbsent().query().exec();
 * 
 * @author Claus Stadler, Dec 29, 2018
 *
 * @param <T>
 */
public interface FacetValueBuilder<T extends RDFNode> {
	FacetDirNode parent();
	FacetValueBuilder<FacetValueCount> withCounts();

	<X extends RDFNode> FacetValueBuilder<X> itemsAs(Class<X> itemClazz);
	
	
	FacetValueBuilder<T> includeAbsent();
	DataQuery<T> query();
}
