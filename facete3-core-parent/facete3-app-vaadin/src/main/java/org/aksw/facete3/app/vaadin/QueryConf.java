package org.aksw.facete3.app.vaadin;

import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class QueryConf {

    private FacetDirNode facetDirNode;
    private FacetedQuery facetedQuery;
    private Node selectedFacet;

    public FacetDirNode getFacetDirNode() {
        return facetDirNode;
    }

    public void setFacetDirNode() {
        this.facetDirNode = facetedQuery.focus()
                .fwd();
    }

    public void setFacetDirNode(FacetDirNode facetDirNode) {
        this.facetDirNode = facetDirNode;
    }

    public FacetedQuery getFacetedQuery() {
        return facetedQuery;
    }

    public Node getSelectedFacet() {
        return selectedFacet;
    }

    public void setSelectedFacet(Node facet) {
        selectedFacet = facet;
    }

    public void setBaseConcept(Concept baseConcept) {
        facetedQuery = facetedQuery.baseConcept(baseConcept);
    }

    private void setEmptyBaseConcept() {
        Concept emptyConcept = ConceptUtils.createConcept();
        facetedQuery = facetedQuery.baseConcept(emptyConcept);
    }

    public QueryConf(RDFConnection connection) {
        initJena();
        initFacetedQuery(connection);
        setEmptyBaseConcept();
        setFacetDirNode();
        setSelectedFacet(RDF.type.asNode());
    }

    private void initJena() {
        JenaSystem.init();
        JenaPluginFacete3.init();
    }

    private void initFacetedQuery(RDFConnection connection) {
        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery xFacetedQuery = dataModel.createResource()
                .as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(xFacetedQuery);
        facetedQuery = FacetedQueryImpl.create(xFacetedQuery, connection);
    }
}

