package org.aksw.facete3.app.vaadin;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.compare.QueryExecutionFactoryCompare;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.OpExecutorDefault;
import org.aksw.jena_sparql_api.conjure.dataset.engine.TaskContext;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithBnodeRewrite;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithLocalCache;
import org.aksw.jenax.vaadin.label.LabelServiceSwitchable;
import org.aksw.jenax.vaadin.label.LabelServiceSwitchableImpl;
import org.aksw.jenax.vaadin.label.VaadinLabelMgr;
import org.apache.jena.graph.Node;
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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import io.reactivex.rxjava3.core.Flowable;

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


    @Bean
    @Autowired
    public ResourceHolder dataRefEndpoint(EndpointConfig cfg) {
        if (logger.isDebugEnabled()) {
            logger.debug("Created new resource");
        }
        Facete3Wrapper.initJena();

        String sparqlEndpoint = cfg.getSparqlEndpoint();
        ResourceHolder result = new ResourceHolder();

        if (sparqlEndpoint != null) {
            RdfDataRef dataRef = ModelFactory.createDefaultModel().createResource().as(RdfDataRefSparqlEndpoint.class)
                    .setServiceUrl(cfg.getSparqlEndpoint());

            result.set(OpDataRefResource.from(dataRef));
        }
        return result;
    }

    @RefreshScope
    @Bean //(destroyMethod = "close")
    @Autowired
    public RdfDataSource getDataSource(ResourceHolder opHolder) {
        Resource r = opHolder.get();
        Op op = (Op)r;// JenaPluginUtils.polymorphicCast(r);
        RdfDataSource result = createDataSource(op);
//        if (dataRef.getServiceUrl() == null) {
//            result = RDFConnectionFactory.connect(DatasetFactory.create());
//        } else {
//            result = getBaseDataConnection(dataRef);
//        }
        return result;
    }

//    public static RdfDataSource createDataSource(Op op) {
//        RdfDataSource result = () -> {
//            try {
//                return getBaseDataConnection(op);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        };
//        return result;
//    }


//    @RefreshScope
//    @Bean(destroyMethod = "close")
//    @Autowired
//    public RDFConnection getConnection(ResourceHolder opHolder) throws IOException {
//        Resource r = opHolder.get();
//        Op op = (Op)r;// JenaPluginUtils.polymorphicCast(r);
//        RDFConnection result = getBaseDataConnection(op);
////        if (dataRef.getServiceUrl() == null) {
////            result = RDFConnectionFactory.connect(DatasetFactory.create());
////        } else {
////            result = getBaseDataConnection(dataRef);
////        }
//        return result;
//    }

    /**
     *
     * @implNote
     *   The return type must be LabelServiceSwitchable rather than LabelService.
     *   Otherwise, the returned proxy (due to @RefreshScope) won't implement the LabelServiceSwitchable interface,
     *   which e.g. the label switcher needs.
     * @param dataSource
     * @return
     */
    @RefreshScope
    @Bean
    @Autowired
    public LabelServiceSwitchable<Node, String> labelMgr(RdfDataSource dataSource) {
        QueryExecutionFactory qef = dataSource.asQef(); // new QueryExecutionFactoryOverSparqlQueryConnection(conn); // RDFConnection.connect(dataset);
        Property labelProperty = RDFS.label;// DCTerms.description;

        LookupService<Node, String> ls1 = LabelUtils.getLabelLookupService(qef, labelProperty, DefaultPrefixes.get(), 50);
        LookupService<Node, String> ls2 = keys -> Flowable.fromIterable(keys).map(k -> Map.entry(k, Objects.toString(k)));

        // VaadinRdfLabelMgr labelService = new VaadinRdfLabelMgrImpl(LabelUtils.getLabelLookupService(qef, labelProperty, DefaultPrefixes.get(), 50));
        VaadinLabelMgr<Node, String> labelMgr = new VaadinLabelMgr<>(ls1);

        LabelServiceSwitchable<Node, String> result = new LabelServiceSwitchableImpl<>(labelMgr);
        result.getLookupServices().addAll(Arrays.asList(ls1, ls2));

        return result;
    }


    public static RdfDataSource createDataSource(Op op) {
        if (op == null) {
            op = OpData.create(ModelFactory.createDefaultModel());
        }
//      RdfDataPod dataPod = DataPods.fromDataRef(dataRef);
//      result = dataPod.openConnection();

//        String serviceUrl = dataRef.getServiceUrl();
//
//        RDFConnectionBuilder rdfConnectionBuilder = new RDFConnectionBuilder(serviceUrl);
//        RDFConnection rdfConnection = rdfConnectionBuilder.getRDFConnection();


        HttpResourceRepositoryFromFileSystemImpl httpRepo;
        try {
            httpRepo = HttpResourceRepositoryFromFileSystemImpl.createDefault();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        // DataPodFactoryAdvancedImpl dataPodFactory = new DataPodFactoryAdvancedImpl(null, opExecutor, httpRepo);
        RdfDataSource dataSourceRaw = op.accept(opExecutor);

        RdfDataSourceWithBnodeRewrite dataSourceBnode = RdfDataSourceWithBnodeRewrite.wrapWithAutoBnodeProfileDetection(dataSourceRaw);
        RdfDataSourceWithLocalCache dataSourceCache = new RdfDataSourceWithLocalCache(dataSourceBnode);

        QueryExecutionFactory qef = new QueryExecutionFactoryCompare(dataSourceCache.asQef(), dataSourceBnode.asQef());
        RdfDataSource comparingDataSource = RdfDataEngines.adapt(qef);


        // RdfDataSource dataSource = dataSourceCache;
        // RdfDataSource dataSource = dataSourceBnode;
        RdfDataSource dataSource = comparingDataSource;


        // RdfDataSource dataSource = DataPods.from(dataRef);
        // RDFConnection rdfConnection = dataSource.getConnection();

        RdfDataSource result = () -> {
                RDFConnection conn = dataSource.getConnection();
                conn = RDFConnectionUtils.wrapWithQueryTransform(conn,
                    query -> {
                        logger.info("Sending query: " + query);
                        return query;
                    });

                conn = RDFConnectionUtils.wrapWithQueryTransform(conn,
                        query -> QueryUtils.applyOpTransform(query,
                                xop -> Transformer.transform(new TransformExpandAggCountDistinct(), xop)));

                return conn;
            };
        return result;
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
