package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.rdf.model.RDFNode;

public interface DataQuery2<T extends RDFNode>
	extends DataQuery<T>
{
	FacetedQuery toFacetedQuery();
}
