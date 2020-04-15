package org.aksw.facete3.app.vaadin;

import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.apache.jena.graph.Node;

public class QueryConf {
    private FacetDirNode facetDirNode;
    private FacetedQuery facetedQuery;
    private Node selectedFacet;

    public Node getSelectedFacet() {
        return selectedFacet;
    }

    public FacetedQuery getFacetedQuery() {
        return facetedQuery;
    }

    public void setFacetedQuery(FacetedQuery facetedQuery) {
        this.facetedQuery = facetedQuery;
    }

    public FacetDirNode getFacetDirNode() {
        return facetDirNode;
    }

    public void setFacetDirNode(FacetDirNode facetDirNode) {
        this.facetDirNode = facetDirNode;
    }

    public void setSelectedFacet(Node selectedFacet) {
        this.selectedFacet = selectedFacet;
    }

    public QueryConf(FacetDirNode facetDirNode, FacetedQuery facetedQuery, Node selectedFacet) {
        this.facetDirNode = facetDirNode;
        this.facetedQuery = facetedQuery;
        this.selectedFacet = selectedFacet;
    }

}
