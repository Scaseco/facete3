package org.aksw.facete3.app.vaadin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;

public class RDFConnectionBuilder {

    private RDFConnection connection;
    private Config config;

    public RDFConnection getRDFConnection() {
        return connection;
    }

    public RDFConnectionBuilder(Config config) {
        this.config = config;
        // connectFile();
        connectUrl();
        connectCache();
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
