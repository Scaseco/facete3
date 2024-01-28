package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.vaadin.flow.data.provider.Query;

public class FacetValueCountDataProvider
    extends FacetDataProvider<FacetValueCount>
{
    private static final long serialVersionUID = 1448114317952863859L;

    /** The facetDirNode is used to match all facets at a given path */
    protected FacetDirNode facetDirNode;

    /** The selected facet restricts all facets to a specific one */
    protected Node selectedFacet;

    public FacetValueCountDataProvider(Facete3Wrapper facete3, LookupService<Node, String> labelService) {
        super(facete3, labelService);
        this.facetDirNode = facete3.getFacetedQuery().root().fwd();
        this.selectedFacet = RDF.type.asNode();
    }

    @Override
    public DataQuery<FacetValueCount> translateQuery(Query<FacetValueCount, Void> query) {
        DataQuery<FacetValueCount> dataQuery = facetDirNode
                .facetValueCountsWithAbsent(false)
                .only(selectedFacet);
        String filterText = getFilter();
        if (!filterText.isEmpty()) {
            Fragment1 filter = KeywordSearchUtils.createConceptExistsRegexIncludeSubject(
                    Fragment2Impl.create(RDFS.label), filterText);
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
        return selectedFacet;
    }

    public void setSelectedFacet(Node selectedFacet) {
        this.selectedFacet = selectedFacet;
    }

    public void setFacetDirNode(FacetDirNode facetDirNode) {
        this.facetDirNode = facetDirNode;
    }

    public FacetDirNode getFacetDirNode() {
        return facetDirNode;
    }

    public boolean isActive(FacetValueCount facetValueCount) {
        return facete3.getHLFacetConstraint(facetValueCount)
                .isActive();
    }
}
