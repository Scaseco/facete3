package org.aksw.facete3.app.vaadin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class Facete3Wrapper {

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

    public Facete3Wrapper(RDFConnection connection) {
        initJena();
        initFacetedQuery(connection);
        // TODO Change back
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

    public void setConstraints(Set<FacetValueCount> facetValueCount, boolean isEnabled) {
        for (FacetValueCount facet : facetValueCount) {
            Node v = facet.getValue();
            HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp =
                    facetDirNode.via(facet.getPredicate())
                            .one()
                            .constraints()
                            .eq(v);
            tmp.setActive(isEnabled);
        }
    }

    public void setFacetDirection(org.aksw.facete.v3.api.Direction direction) {
        facetDirNode = facetDirNode.parent()
                .step(direction);
    }

    public void resetPath() {
        FacetNode rootNode = facetDirNode.parent()
                .root();
        changeFocus(rootNode);
    }

    public void addFacetToPath(FacetCount facet) {
        org.aksw.facete.v3.api.Direction dir = facetDirNode.dir();
        Node node = facet.getPredicate();
        facetedQuery.focus()
                .step(node, dir)
                .one()
                .chFocus();
        setFacetDirNode(facetedQuery.focus()
                .step(dir));
    }

    public void changeFocus(FacetNode node) {
        org.aksw.facete.v3.api.Direction dir = node.reachingDirection();
        if (dir == null) {
            dir = facetDirNode.dir();
        }
        node.chFocus();
        setFacetDirNode(node.step(dir));
    }

    public List<Node> getPathNodes() {
        List<Directed<FacetNode>> path = facetDirNode.parent()
                .path();
        List<Node> pathNodes = path.stream()
                .map(Directed::getValue)
                .map(FacetNode::reachingPredicate)
                .collect(Collectors.toList());
        return pathNodes;
    }

    public List<Directed<FacetNode>> getPath() {
        return facetDirNode.parent()
                .path();
    }

    // TODO Should not be here
    public RDFNode fetchIfResource(Node node) {
        Query query = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");
        UnaryRelation filter = ConceptUtils.createFilterConcept(node);
        query.setQueryPattern(RelationImpl.create(query.getQueryPattern(), Vars.s)
                .joinOn(Vars.s)
                .with(filter)
                .getElement());
        Model model = facetedQuery.connection()
                .queryConstruct(query);
        RDFNode result = model.asRDFNode(node);
        return result;
    }
}

