package org.aksw.facete3.app.vaadin;

import java.io.IOException;
import java.util.HashMap;

import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPodFactoryAdvancedImpl;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactoryOverSparqlQueryConnection;
import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.arq.datasource.RdfDataSourceWithLocalCache;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.vaadin.label.VaadinRdfLabelMgr;
import org.aksw.jenax.vaadin.label.VaadinRdfLabelMgrImpl;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.vocabulary.RDFS;
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
    public ResourceHolder dataRefEndpoint(EndpointConfig cfg) {
        System.out.println("Created new resource");
        Facete3Wrapper.initJena();

        RdfDataRef dataRef = ModelFactory.createDefaultModel().createResource().as(RdfDataRefSparqlEndpoint.class)
            .setServiceUrl(cfg.getSparqlEndpoint());

        ResourceHolder result = new ResourceHolder();
        result.set(OpDataRefResource.from(dataRef));
        return result;
    }



    @RefreshScope
    @Bean(destroyMethod = "close")
    @Autowired
    public RDFConnection getConnection(ResourceHolder opHolder) throws IOException {
        Resource r = opHolder.get();
        Op op = (Op)r;// JenaPluginUtils.polymorphicCast(r);
        RDFConnection result = getBaseDataConnection(op);
//        if (dataRef.getServiceUrl() == null) {
//            result = RDFConnectionFactory.connect(DatasetFactory.create());
//        } else {
//            result = getBaseDataConnection(dataRef);
//        }
        return result;
    }

    @RefreshScope
    @Bean
    @Autowired
    public VaadinRdfLabelMgr labelMgr(RDFConnection conn) {
        QueryExecutionFactory qef = new QueryExecutionFactoryOverSparqlQueryConnection(conn); // RDFConnection.connect(dataset);
        Property labelProperty = RDFS.label;// DCTerms.description;
        VaadinRdfLabelMgr labelService = new VaadinRdfLabelMgrImpl(LabelUtils.getLabelLookupService(qef, labelProperty, DefaultPrefixes.get()));
        return labelService;
    }


    public static RDFConnection getBaseDataConnection(Op op) throws IOException {
//      RdfDataPod dataPod = DataPods.fromDataRef(dataRef);
//      result = dataPod.openConnection();

//        String serviceUrl = dataRef.getServiceUrl();
//
//        RDFConnectionBuilder rdfConnectionBuilder = new RDFConnectionBuilder(serviceUrl);
//        RDFConnection rdfConnection = rdfConnectionBuilder.getRDFConnection();

        HttpResourceRepositoryFromFileSystemImpl httpRepo = HttpResourceRepositoryFromFileSystemImpl.createDefault();
        TaskContext taskContext = new TaskContext(null, new HashMap<>(), new HashMap<>());

//        Model repoUnionModel = repoDataset.getUnionModel();
//        taskContext.getCtxModels().put("thisCatalog", repoUnionModel);

        OpVisitor<RdfDataPod> opExecutor = new OpExecutorDefault(
                null,
                httpRepo,
                // httpRepo.getCacheStore(),
                taskContext,
                new HashMap<>(),
                // srcFileNameRes,
                RDFFormat.TURTLE_BLOCKS);
        DataPodFactoryAdvancedImpl dataPodFactory = new DataPodFactoryAdvancedImpl(null, opExecutor, httpRepo);


        RdfDataSource dataSourceRaw = op.accept(opExecutor);

        RdfDataSourceWithBnodeRewrite dataSourceBnode = RdfDataSourceWithBnodeRewrite.wrapWithAutoBnodeProfileDetection(dataSourceRaw);
        RdfDataSourceWithLocalCache dataSourceCache = new RdfDataSourceWithLocalCache(dataSourceBnode);

        RdfDataSource dataSource = dataSourceCache;

        // RdfDataSource dataSource = DataPods.from(dataRef);
        RDFConnection rdfConnection = dataSource.getConnection();

        rdfConnection = RDFConnectionUtils.wrapWithQueryTransform(rdfConnection,
                query -> {
                    logger.info("Sending query: " + query);
                    return query;
                });

        rdfConnection = RDFConnectionUtils.wrapWithQueryTransform(rdfConnection,
                query -> QueryUtils.applyOpTransform(query,
                        xop -> Transformer.transform(new TransformExpandAggCountDistinct(), xop)));

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
