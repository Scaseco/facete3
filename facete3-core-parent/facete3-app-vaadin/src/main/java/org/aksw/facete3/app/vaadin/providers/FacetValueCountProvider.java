package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;
import com.vaadin.flow.data.provider.Query;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;

public class FacetValueCountProvider extends FacetProvider<FacetValueCount> {

    private static final long serialVersionUID = 1448114317952863859L;

    public FacetValueCountProvider(QueryConf queryConf) {
        super(queryConf);
    }

    @Override
    protected DataQuery<FacetValueCount> translateQuery(Query<FacetValueCount, Void> query) {
        DataQuery<FacetValueCount> dataQuery = queryConf.getFacetDirNode()
                .facetValueCountsWithAbsent(false)
                .only(queryConf.getSelectedFacet());
        String filterText = getFilter();
        if (!filterText.isEmpty()) {
            UnaryRelation filter = KeywordSearchUtils.createConceptRegexIncludeSubject(
                    BinaryRelationImpl.create(RDFS.label), filterText);
            dataQuery.filterUsing(filter, FacetValueCountImpl_.VALUE);
        }
        return dataQuery;
    }

    @Override
    public Object getId(FacetValueCount facetValueCount) {
        return facetValueCount.toString()
                .hashCode();
    }

    @Override
    protected Function<? super FacetValueCount, ? extends Node> getNodeForLabelFunction() {
        return FacetValueCount::getValue;
    }

    public Node getSelectedFacet() {
        return queryConf.getSelectedFacet();
    }
}
