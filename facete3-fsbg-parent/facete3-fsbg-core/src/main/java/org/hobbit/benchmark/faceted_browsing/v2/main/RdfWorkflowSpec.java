package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.aksw.jena_sparql_api.core.RDFConnectionEx;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.RDFConnectionMetaData;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Base class for SPARQL-based operators.
 *
 * The result of a SPARQL operation is an RDF connection.
 *
 */
interface OpSparql {
    RDFConnection apply();
}


class OpLoadFileOrUrl {
    protected String filenameOrURL;


}

/**
 * There are three types of caching patterns:
 * Result objects can be consumable and non-consumable
 *
 * - Create a Model, write it to a file (possibly async if readonly), hand it to the application
 *   - (It would also be possible to read from cache)
 * - Create an consumable resource, write to cache, then read from cache
 * - Create an consumable resource, and wrap it so that items get serialized to the file
 *
 *
 * @author raven
 *
 * @param <T>
 */
class ModelCreationImpl<T>
    implements ModelCreation<T>
{
    private static final Logger logger = LoggerFactory.getLogger(RdfWorkflowSpec.class);

    protected Supplier<String> datasetIri;
    protected String cacheId;
    protected Supplier<? extends T> resultSupplier;

    protected BiConsumerWithException<? super Path, ? super T> resultSerializer;
    protected FunctionWithException<? super Path, ? extends T> resultDeserializer;

//	// File with CONSTRUCT queries
//	protected String sparqlFilenameOrUri;

    //protected Map<SparqlServiceReference, Function<SparqlServiceReference, RDFConnection>> rdfConnectionFactoryRegistry;

    protected boolean allowCacheRead;
    protected boolean allowCacheWrite;
    protected Path repoRoot;

    public static interface BiConsumerWithException<T, U> {
        void accept(T t, U u) throws Exception;
    }

    public static interface FunctionWithException<T, R> {
        R apply(T t) throws Exception;
    }

    public ModelCreationImpl(
            Supplier<String> datasetIri,
            String cacheId,
            //boolean isResultExhaustable,
            Supplier<? extends T> resultSupplier,
            BiConsumerWithException<? super Path, ? super T> resultSerializer,
            FunctionWithException<? super Path, ? extends T> resultDeserializer) {//Map<SparqlServiceReference, Function<SparqlServiceReference, RDFConnection>> rdfConnectionFactoryRegistry) {
        super();
        this.datasetIri = datasetIri;
        //this.ssr = ssr;
        //this.conn = conn;
        //this.sparqlFilenameOrUri = sparqlFilenameOrUri;
        this.cacheId = cacheId;
        this.resultSupplier = resultSupplier;

        this.resultSerializer = resultSerializer;
        this.resultDeserializer = resultDeserializer;

        //this.rdfConnectionFactoryRegistry = rdfConnectionFactoryRegistry;

        String userDirStr = StandardSystemProperty.USER_HOME.value();

        this.repoRoot = Paths.get(userDirStr).resolve(".dcat/repository/derived");
    }


    /**
     * Return an existing or allocate a new id for a
     * dcat:dataset description based on the connection metadat.
     * If the connection is known to refer to a specific dataset, return its identifier.
     * Otherwise, create an anonymous dataset with a dcat sparql-distribution that
     * represents the connection
     *
     *
     *
     * @param conn
     * @return
     */
    public static String deriveDatasetIri(RDFConnectionEx conn) {
        RDFConnectionMetaData metadata = conn.getMetaData();

        Resource dataset = Iterables.getFirst(metadata.getDatasets(), null);
//				.orElseThrow(() -> new RuntimeException("RDFConnection must be configured with a dataset identifier"));

        String result = dataset != null && dataset.isURIResource() ? dataset.getURI() : null;

        // serviceURL may be null if we are dealing with an in-memory model
        if(result == null) {

            // TODO - We'd need to know which distribution the connection corresponds to
            // Maybe the connection has to return a pair (dataset - distribution)?
            // (or a distribution with a link back to the dataset
            //SparqlDistribution sparqlDistribution = deriveDcatDistribution(conn);

            String serviceUrl = metadata.getServiceURL();
            if(serviceUrl == null) {
                // we cannot do caching without an identifier to relate to
                throw new RuntimeException("no service url");
                //serviceUrl = "absent-service-url";
            }

            DatasetDescription dd = new DatasetDescription(
                    metadata.getDefaultGraphs(),
                    metadata.getNamedGraphs());

            //DatasetDescription dd = createDatasetDescriptionFromSparqlDistribution(sparqlDistribution);
            String graphsHash = createHashForDatasetDescription(dd);

            result = serviceUrl + "/" + graphsHash;
            //String datasetIri = deriveDatasetIri(conn);


//			// TODO Replace RDFConnectionMetaData.class with
//			// SomeDistribution.class
//			Model model = ModelFactory.createDefaultModel();
//			RDFConnectionMetaData distribution = model.createResource()
//					.as(RDFConnectionMetaData.class);
//
//			String serviceUrl = metadata.getServiceURL();
//			distribution.setServiceURL(serviceUrl);
//
//			// If there is a dataset, relate derived information to it
//			// Otherwise, use the connection details as a dataset id
//			distribution.getDefaultGraphs().addAll(metadata.getDefaultGraphs());
//			distribution.getNamedGraphs().addAll(metadata.getNamedGraphs());
//
//
//
//			// TODO Probably add a resolver-lambda for resolving a relative path
//			//String prefix = "http://www.example.org/";
//
//			// Create a dataset id if we do not have one
//			if(serviceUrl == null) {
//
//			}
//
//			Resource dataset = model.createResource();//.as();
//
//
//			result = distribution;
        }

        return result;
    }

    public static DatasetDescription createDatasetDescriptionFromSparqlDistribution(Resource r) {
        SparqlDistribution distribution = r.as(SparqlDistribution.class);

        DatasetDescription result = new DatasetDescription(
                distribution.getDefaultGraphs(),
                distribution.getNamedGraphs());

        return result;
    }

    public ModelCreation<T> cache(boolean onOrOff) {
        allowCacheRead = onOrOff;
        allowCacheWrite = onOrOff;

        return this;
    }

    public static String createHashForSparqlQueryFile(PrefixMapping pm, String sparqlFilenameOrUri) throws FileNotFoundException, IOException, ParseException {
        PrefixMapping pmCopy = new PrefixMappingImpl();
        pmCopy.setNsPrefixes(pm);

        String str = Streams.stream(SparqlStmtUtils.processFile(pmCopy, sparqlFilenameOrUri))
                .map(Object::toString)
                .collect(Collectors.joining("\n"));

        String result = StringUtils.md5Hash(str);

        return result;
    }

    public static String createHashForDatasetDescription(DatasetDescription ds) {
        String result = StringUtils.md5Hash("" + ds);
        return result;
    }

    public T getModel() throws Exception {
        T result;

        // Create a hash from the sparqlFilename
        //String datasetIri = deriveDatasetIri(conn);
        //String queriesHash = //createHashForSparqlQueryFile(PrefixMapping.Extended, sparqlFilenameOrUri);

        // Full cache id is based on ssr + hash
        Path cacheFile = null;
        if(allowCacheRead) {
            String d = datasetIri.get();
            Path relPath = CatalogResolverFilesystem
                    .resolvePath(d)
    //				.resolve(graphsHash)
                    .resolve(cacheId);



            //Path root = Paths.get("/tmp");
            Path cacheFolder = repoRoot.resolve(relPath);

            Files.createDirectories(cacheFolder);

            cacheFile = cacheFolder.resolve("data.ttl");
            logger.debug("Loading data from cache " + cacheFile);
        }

        if(allowCacheRead && Files.exists(cacheFile)) {
            // Lookup with key
            //result = RDFDataMgr.loadModel(cacheFile.toUri().toString());
            result = resultDeserializer.apply(cacheFile);

        } else {

//			Function<SparqlServiceReference, RDFConnection> defaultConnFactory = rdfConnectionFactoryRegistry.get(null);
//
//			Function<SparqlServiceReference, RDFConnection> connFactory =
//					rdfConnectionFactoryRegistry.getOrDefault(ssr, defaultConnFactory);

//			try(RDFConnection conn = connFactory.apply(ssr)) {
                result = resultSupplier.get();


//			}

            if(allowCacheWrite) {
                resultSerializer.accept(cacheFile, result);

                //RDFDataMgr.write(new FileOutputStream(cacheFile.toFile()), result, RDFFormat.TURTLE);

                result = resultDeserializer.apply(cacheFile);
            }
        }

        return result;
    }
}

interface ModelFile {
    Model load();
}

public class RdfWorkflowSpec {

    protected Map<SparqlServiceReference, Function<SparqlServiceReference, RDFConnection>> rdfConnectionFactoryRegistory = new LinkedHashMap<>();

    public RdfWorkflowSpec registerConnectionFactory(SparqlServiceReference ssr, Function<SparqlServiceReference, RDFConnection> fn) {
        rdfConnectionFactoryRegistory.put(ssr, fn);
        return this;
    }

    public RdfWorkflowSpec setDefaultConnectionFactory(Function<SparqlServiceReference, RDFConnection> fn) {
        rdfConnectionFactoryRegistory.put(null, fn);
        return this;
    }

    // This creates an operator instance
    public ModelCreation<Model> deriveDatasetWithSparql(SparqlServiceReference ssr, String filenameOrUri) {
        RDFConnectionEx conn = RDFConnectionFactoryEx.connect(ssr);
        //return new ModelCreationImpl(ssr, filenameOrUri, rdfConnectionFactoryRegistory);
        ModelCreation<Model> result = deriveDatasetWithSparql(conn, filenameOrUri);
        return result;
    }


    public ModelCreationImpl<Flowable<Resource>> execFlowable(RDFConnectionEx conn, Entry<Node, Query> partitionedQuery) {
        String cacheId = "sparql-query/flowable/" + StringUtils.md5Hash("" + partitionedQuery);

        return new ModelCreationImpl<Flowable<Resource>>(
                () -> ModelCreationImpl.deriveDatasetIri(conn),
                cacheId,
                () -> SparqlRx.execPartitioned(conn, partitionedQuery).map(RDFNode::asResource),
                (cacheFile, result) -> RDFDataMgrRx.writeResources(result, cacheFile, RDFFormat.TRIG),
                cacheFile -> RDFDataMgrRx.createFlowableResources(() -> new FileInputStream(cacheFile.toFile()), Lang.TRIG, cacheFile.toAbsolutePath().toString())
            );
    }


    public ModelCreation<Model> execConstruct(RDFConnectionEx conn, String queryStr) {
        String cacheId = "sparql-query/construct/" + StringUtils.md5Hash(queryStr);

        //return new ModelCreationImpl(conn, cacheId, () -> conn.queryConstruct(queryStr));
        return new ModelCreationImpl<Model>(
                () -> ModelCreationImpl.deriveDatasetIri(conn),
                cacheId,
                () -> conn.queryConstruct(queryStr),
                (cacheFile, result) -> RDFDataMgr.write(new FileOutputStream(cacheFile.toFile()), result, RDFFormat.TURTLE),
                cacheFile -> RDFDataMgr.loadModel(cacheFile.toUri().toString())
            );
    }


    public ModelCreation<Model> deriveDatasetWithSparql(RDFConnectionEx conn, String sparqlFilenameOrUri) {
        String cacheId;
        try {
            cacheId = ModelCreationImpl.createHashForSparqlQueryFile(PrefixMapping.Extended, sparqlFilenameOrUri);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        //String datasetIri = ModelCreationImpl.deriveDatasetIri(conn);

        return new ModelCreationImpl<Model>(
                () -> ModelCreationImpl.deriveDatasetIri(conn),
                cacheId,
                () -> RDFDataMgrEx.execConstruct(conn, sparqlFilenameOrUri),
                (cacheFile, result) -> RDFDataMgr.write(new FileOutputStream(cacheFile.toFile()), result, RDFFormat.TURTLE),
                cacheFile -> RDFDataMgr.loadModel(cacheFile.toUri().toString())
            );

//		return new ModelCreationImpl(conn, cacheId, () -> {
//			return RDFDataMgrEx.execConstruct(conn, sparqlFilenameOrUri);
//		});
    }

    public ModelCreation<Model> deriveDatasetWithFunction(RDFConnectionEx conn, String cacheId, Supplier<? extends Model> modelSupplier) {//Function<? super RDFConnectionEx, ? extends Model> modelSupplier) {
        return new ModelCreationImpl<Model>(
                () -> ModelCreationImpl.deriveDatasetIri(conn),
                cacheId,
                modelSupplier,
                (cacheFile, result) -> RDFDataMgr.write(new FileOutputStream(cacheFile.toFile()), result, RDFFormat.TURTLE),
                cacheFile -> RDFDataMgr.loadModel(cacheFile.toUri().toString())
                );
    }

}
