package org.aksw.facete3.app.vaadin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class QueryConf {

    private Config config;
    private FacetDirNode facetDirNode;
    private FacetedQuery facetedQuery;
    private Node selectedFacet;
    private RDFConnection connection;

    public RDFConnection getConnection() {
        return connection;
    }

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

    public void setSelectedFacet() {
        selectedFacet = RDF.type.asNode();
    }

    public void setSelectedFacet(Node facet) {
        selectedFacet = facet;
    }

    public void setBaseConcept(Concept baseConcept) {
        facetedQuery = facetedQuery.baseConcept(baseConcept);
    }

    public void setBaseConcept() {
        Concept emptyConcept = ConceptUtils.createConcept();
        facetedQuery = facetedQuery.baseConcept(emptyConcept);
    }

    public QueryConf(Config config) {
        this.config = config;
        initJena();
        setConnection();
        setFacetedQuery();
        setBaseConcept();
        setFacetDirNode();
        setSelectedFacet();
    }

    private void initJena() {
        JenaSystem.init();
        JenaPluginFacete3.init();
    }

    private void setConnection() {
        // connectFile();
        connectUrl();
        connectCache();
    }

    private void setFacetedQuery() {
        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery xFacetedQuery = dataModel.createResource()
                .as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(xFacetedQuery);
        facetedQuery = FacetedQueryImpl.create(xFacetedQuery, connection);
    }

    private void connectUrl() {
        String sparqlEnpoint = config.getSparqlEnpoint();
        connection = RDFConnectionRemote.create()
                .destination(sparqlEnpoint)
                .acceptHeaderQuery(WebContent.contentTypeResultsXML)
                .build();
    }

    private void connectCache() {
        connection =
                new RDFConnectionModular(new SparqlQueryConnectionJsa(FluentQueryExecutionFactory
                        .from(new QueryExecutionFactorySparqlQueryConnection(connection))
                        .config()
                        .withCache(new CacheBackendMem())
                        .end()
                        .create()), connection, connection);
    }

    private void connectFile() {
        Dataset dataset = DatasetFactory.create();
        RDFDataMgr.read(dataset,
                "/home/beavis/cloud/repositories/link-discovery-and-data-fusion/fusion/fused.nt");
        RDFConnection conn = RDFConnectionFactory.connect(dataset);
        connection = wrapWithVirtualBnodeUris(conn, "jena");
    }

    public static RDFConnection wrapWithVirtualBnodeUris(RDFConnection conn, String profile) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        RDFDataMgrEx.execSparql(model, "udf-inferences.sparql");
        Set<String> activeProfiles =
                new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris xform =
                ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);
        RDFConnection result = RDFConnectionFactoryEx.wrapWithQueryTransform(conn, xform::rewrite);
        return result;
    }

    public static RDFConnection wrapWithFilter(RDFConnection conn, String profile) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        RDFDataMgrEx.execSparql(model, "udf-inferences.sparql");
        Set<String> activeProfiles =
                new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris xform =
                ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);
        RDFConnection result = RDFConnectionFactoryEx.wrapWithQueryTransform(conn, xform::rewrite);
        return result;
    }
}

