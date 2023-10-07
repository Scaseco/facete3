package org.aksw.facete3.app.vaadin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsa;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryOverSparqlQueryConnection;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;

public class RDFConnectionBuilder {

    private RDFConnection connection;
//    private Config config;

    public RDFConnection getRDFConnection() {
        return connection;
    }

    public RDFConnectionBuilder(String serviceUrl) {
//        this.config = config;
        // connectFile();
        connectUrl(serviceUrl);
        connectCache();
    }

    private void connectUrl(String sparqlEndpoint) {
//        String sparqlEnpoint = config.getSparqlEnpoint();
        connection = RDFConnectionRemote.create()
                .destination(sparqlEndpoint)
                .acceptHeaderQuery(WebContent.contentTypeResultsXML)
                .build();
    }

    private void connectCache() {
        connection =
                new RDFConnectionModular(new SparqlQueryConnectionJsa(FluentQueryExecutionFactory
                        .from(new QueryExecutionFactoryOverSparqlQueryConnection(connection))
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
        SparqlStmtMgr.execSparql(model, "udf-inferences.sparql");
        Set<String> activeProfiles =
                new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris xform =
                ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);
        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(conn, xform::rewrite);
        return result;
    }

    public static RDFConnection wrapWithFilter(RDFConnection conn, String profile) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.sparql");
        Set<String> activeProfiles =
                new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
        ExprTransformVirtualBnodeUris xform =
                ExprTransformVirtualBnodeUris.createTransformFromUdfModel(model, activeProfiles);
        RDFConnection result = RDFConnectionUtils.wrapWithQueryTransform(conn, xform::rewrite);
        return result;
    }
}
