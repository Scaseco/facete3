package org.aksw.facete3.app.shared.concept;

import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.PartitionedQuery;
import org.apache.jena.rdf.model.RDFNode;

public interface RDFNodeSpec {
    boolean isCollection();
    RDFNodeSpecFromCollection asCollection();

    boolean isPartitionedQuery();
    RDFNodeSpecFromPartitionedQuery asPartitionedQuery();


    default PartitionedQuery getPartitionedQuery() {
        return asPartitionedQuery().getPartitionedQuery();
    }

    default Collection<? extends RDFNode> getCollection() {
        return asCollection().getRDFNodes();
    }
}
