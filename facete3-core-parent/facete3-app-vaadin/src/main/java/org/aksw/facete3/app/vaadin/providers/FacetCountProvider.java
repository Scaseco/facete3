package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class FacetCountProvider extends FacetProvider<FacetCount> {

    private static final long serialVersionUID = 12L;

    public FacetCountProvider(QueryConf queryConf) {
        super(queryConf);
    }


    @Override
    protected DataQuery<FacetCount> translateQuery(Query<FacetCount, Void> query) {
        DataQuery<FacetCount> dataQuery = queryConf.getFacetedQuery()
                .focus()
                .fwd()
                .facetCounts()
                .exclude(RDF.type);
        String filterText = getFilter();
        if (!filterText.isEmpty()) {
            UnaryRelation filter = KeywordSearchUtils.createConceptRegexIncludeSubject(
                    BinaryRelationImpl.create(RDFS.label), filterText);
            dataQuery.filter(filter);
        }
        return dataQuery;
    }


    @Override
    protected Function<? super FacetCount, ? extends Node> getNodeFunction() {
        return FacetCount::getPredicate;
    }
}

