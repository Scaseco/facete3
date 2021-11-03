package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.algebra.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A configuration that features beans for configuring a connection to a SPARQL endpoint as
 * well as the connection itself.
 * The configuration makes use of the @{@link RefreshScope} annotation to enable refreshing the connection on
 * configuration change.
 * If a context is properly setup then a refresh can be triggered manually
 * using {@code cxt.getBean(RefreshScope.class).refreshAll()}.
 *
 * @author raven
 *
 */
public class ConfigEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ConfigEndpoint.class);


//    @Bean
//    public CustomScopeConfigurer servletCustomScopeConfigurer(org.springframework.cloud.context.scope.refresh.RefreshScope refreshScope) {
//        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
////        customScopeConfigurer.addScope("refresh", refreshScope);
//        customScopeConfigurer.addScope("refresh", new org.springframework.cloud.context.scope.refresh.RefreshScope());
//        return customScopeConfigurer;
//    }


    @Configuration
    @ConfigurationProperties("facete3")
    public static class EndpointConfig {
        protected String sparqlEndpoint;

        public String getSparqlEndpoint() {
            return sparqlEndpoint;
        }

        public void setSparqlEndpoint(String sparqlEndpoint) {
            System.out.println("SPARQL ENDPOINT SET TO " + sparqlEndpoint);
            this.sparqlEndpoint = sparqlEndpoint;
        }
    }



    @Bean
    @Autowired
    public DataRefSparqlEndpoint dataRefEndpoint(EndpointConfig cfg) {
        System.out.println("Created new resource");
        Facete3Wrapper.initJena();

        return ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class)
            .setServiceUrl(cfg.getSparqlEndpoint());
    }

    @RefreshScope
    @Bean(destroyMethod = "close")
    @Autowired
    public RDFConnection getConnection(DataRefSparqlEndpoint dataRef) {
        RDFConnection result;
        if (dataRef.getServiceUrl() == null) {
            result = RDFConnectionFactory.connect(DatasetFactory.create());
        } else {
            result = getBaseDataConnection(dataRef);
        }
        return result;
    }


    public static RDFConnection getBaseDataConnection(DataRefSparqlEndpoint dataRef) {
//      RdfDataPod dataPod = DataPods.fromDataRef(dataRef);
//      result = dataPod.openConnection();

        String serviceUrl = dataRef.getServiceUrl();

        RDFConnectionBuilder rdfConnectionBuilder = new RDFConnectionBuilder(serviceUrl);
        RDFConnection rdfConnection = rdfConnectionBuilder.getRDFConnection();

        rdfConnection = RDFConnectionUtils.wrapWithQueryTransform(rdfConnection,
                query -> {
                    logger.info("Sending query:" + query);
                    return query;
                });

        rdfConnection = RDFConnectionUtils.wrapWithQueryTransform(rdfConnection,
                query -> QueryUtils.applyOpTransform(query,
                        op -> Transformer.transform(new TransformExpandAggCountDistinct(), op)));

        return rdfConnection;
    }


//    @Bean
//    @Autowired
    public Runnable testConn(RDFConnection conn) {
//        System.out.println("Creating runnable from connection " + conn);

        return () -> {
            try (QueryExecution qe = conn.query("SELECT (COUNT(*) AS ?c) { ?s a ?o }")) {
                System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        };
    }
}
