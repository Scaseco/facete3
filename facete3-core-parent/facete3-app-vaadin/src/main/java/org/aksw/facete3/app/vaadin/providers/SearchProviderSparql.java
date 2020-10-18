package org.aksw.facete3.app.vaadin.providers;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromPartitionedQuery;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromPartitionedQueryImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.mapper.PartitionedQueryUtils;
import org.apache.jena.vocabulary.RDFS;

public class SearchProviderSparql
    implements SearchProvider
{
    @Override
    public RDFNodeSpec search(String searchString) {
//        UnaryRelation concept = KeywordSearchUtils.createConceptRegexIncludeSubject(
//                BinaryRelationImpl.create(RDFS.label), searchString);
//
//        PartitionedQuery1 pq =
//        RDFNodeSpecFromPartitionedQuery result = RDFNodeSpecFromPartitionedQueryImpl.create(concept);
//        return result;
        return null;
    }

}
