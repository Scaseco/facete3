package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;

import com.vaadin.flow.data.provider.Query;

public class FacetCountProvider extends FacetProvider<FacetCount> {

    private static final long serialVersionUID = 12L;

    public FacetCountProvider(Facete3Wrapper facete3, LookupService<Node, String> labelService) {
        super(facete3, labelService);
    }


    @Override
    public DataQuery<FacetCount> translateQuery(Query<FacetCount, Void> query) {
        DataQuery<FacetCount> dataQuery = facete3.getFacetDirNode()
                .facetCounts();
//                .exclude(RDF.type);

        String filterText = getFilter();
        if (!filterText.isEmpty()) {
            // FIXME Make keyword search strategy configurable
            Fragment1 filter = KeywordSearchUtils.createConceptExistsRegexIncludeSubject(
                    Fragment2Impl.create(RDFS.label), filterText);
            dataQuery.filter(filter);
        }
        return dataQuery;
    }


    @Override
    protected Function<? super FacetCount, ? extends Node> getNodeForLabelFunction() {
        return FacetCount::getPredicate;
    }
}

