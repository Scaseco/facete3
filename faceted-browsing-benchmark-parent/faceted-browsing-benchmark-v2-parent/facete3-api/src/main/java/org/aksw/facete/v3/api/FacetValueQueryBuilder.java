package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.data_query.api.DataQuery;
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
public interface FacetValueQueryBuilder<T extends RDFNode> {
	FacetDirNode parent();
	FacetValueQueryBuilder<FacetValueCount> withCounts();

	<X extends RDFNode> FacetValueQueryBuilder<X> itemsAs(Class<X> itemClazz);
	
	
	FacetValueQueryBuilder<T> includeAbsent();
	DataQuery<T> query();
}
