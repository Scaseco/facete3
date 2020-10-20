package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromRootedQuery;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromRootedQueryImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1Impl;
import org.aksw.jena_sparql_api.mapper.RootedQuery;
import org.aksw.jena_sparql_api.mapper.RootedQueryFromPartitionedQuery1;
import org.apache.jena.query.Query;

public class SearchProviderSparql
    implements SearchProvider
{
    protected Function<String, ? extends UnaryRelation> searchStringToConcept;

    public SearchProviderSparql(Function<String, ? extends UnaryRelation> searchStringToConcept) {
        super();
        this.searchStringToConcept = searchStringToConcept;
    }

    @Override
    public RDFNodeSpec search(String searchString) {
        UnaryRelation concept = searchStringToConcept.apply(searchString);

        Query query = new Query();
        query.setQueryConstructType();
        query.setQueryPattern(concept.getElement());

        PartitionedQuery1 pq = new PartitionedQuery1Impl(query, concept.getVar());
        RootedQuery rq = new RootedQueryFromPartitionedQuery1(pq);
        RDFNodeSpecFromRootedQuery result = new RDFNodeSpecFromRootedQueryImpl(rq);
        return result;
    }

}
