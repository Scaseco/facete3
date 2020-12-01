package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;

import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;

import com.vaadin.flow.data.provider.Query;

public class FacetValueCountProvider extends FacetProvider<FacetValueCount> {

    private static final long serialVersionUID = 1448114317952863859L;

    public FacetValueCountProvider(Facete3Wrapper facete3, LookupService<Node, String> labelService) {
        super(facete3, labelService);
    }

    @Override
    protected DataQuery<FacetValueCount> translateQuery(Query<FacetValueCount, Void> query) {
        DataQuery<FacetValueCount> dataQuery = facete3.getFacetDirNode()
                .facetValueCountsWithAbsent(false)
                .only(facete3.getSelectedFacet());
        String filterText = getFilter();
        if (!filterText.isEmpty()) {
            UnaryRelation filter = KeywordSearchUtils.createConceptExistsRegexIncludeSubject(
                    BinaryRelationImpl.create(RDFS.label), filterText);
            dataQuery.filterUsing(filter, FacetValueCountImpl_.VALUE);
        }
        return dataQuery;
    }

    @Override
    public Object getId(FacetValueCount facetValueCount) {
        return facetValueCount.getValue()
                .hashCode();
    }

    @Override
    protected Function<? super FacetValueCount, ? extends Node> getNodeForLabelFunction() {
        return FacetValueCount::getValue;
    }

    public Node getSelectedFacet() {
        return facete3.getSelectedFacet();
    }

    public boolean isActive(FacetValueCount facetValueCount) {
        return facete3.getHLFacetConstraint(facetValueCount)
                .isActive();
    }
}
