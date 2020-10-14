package org.aksw.facete3.app.shared.concept;

import org.aksw.jena_sparql_api.mapper.PartitionedQuery;

public interface RDFNodeSpecFromPartitionedQuery
    extends RDFNodeSpec
{
    PartitionedQuery getPartitionedQuery();
}
