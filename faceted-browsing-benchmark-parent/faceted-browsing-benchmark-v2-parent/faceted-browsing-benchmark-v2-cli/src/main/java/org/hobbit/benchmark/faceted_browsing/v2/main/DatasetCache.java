package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.dcat.repo.impl.fs.CatalogResolverFilesystem;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.utils.RDFDataMgrEx;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetDescription;


interface ModelCreation {
	Model getModel() throws Exception;
	//Flowable<Triple> execTriples();
	//RDFConnection toTempStore();
	//ModelFile cacheToFile();
	ModelCreation cache();
}

class ModelCreationImpl
	implements ModelCreation
{
	protected SparqlServiceReference ssr;
	
	// File with CONSTRUCT queries
	protected String sparqlFilenameOrUri;

	protected Map<SparqlServiceReference, Function<SparqlServiceReference, RDFConnection>> rdfConnectionFactoryRegistry;
	
	protected boolean allowCacheRead;
	
	protected boolean allowCacheWrite;
	
	
	public ModelCreationImpl(SparqlServiceReference ssr, String sparqlFilenameOrUri, Map<SparqlServiceReference, Function<SparqlServiceReference, RDFConnection>> rdfConnectionFactoryRegistry) {
		super();
		this.ssr = ssr;
		this.sparqlFilenameOrUri = sparqlFilenameOrUri;
		
		this.rdfConnectionFactoryRegistry = rdfConnectionFactoryRegistry;
	}

	public ModelCreation cache() {
		allowCacheRead = true;
		allowCacheWrite = true;
		
		return this;
	}
	
	public Model getModel() throws Exception {
		Model result;
		// Create a hash from the sparqlFilename
		String str;
		
		str = SparqlStmtUtils.processFile(PrefixMapping.Extended, sparqlFilenameOrUri)
				.map(Object::toString)
				.collect(Collectors.joining("\n"));

		String queriesHash = StringUtils.md5Hash(str);

		String graphsHash = StringUtils.md5Hash("" + ssr.getDatasetDescription());
		// Full cache id is based on ssr + hash
		Path relPath = CatalogResolverFilesystem.resolvePath(ssr.getServiceURL()).resolve(graphsHash);

		Path root = Paths.get("/tmp");
		Path cacheFolder = root.resolve(relPath);
		
		Path cacheFile = cacheFolder.resolve("data.ttl");
		
		if(allowCacheRead && Files.exists(cacheFile)) {
			// Lookup with key
			result = RDFDataMgr.loadModel(cacheFile.toUri().toString());
		} else {
			
			Function<SparqlServiceReference, RDFConnection> connFactory = rdfConnectionFactoryRegistry.get(ssr);

			try(RDFConnection conn = connFactory.apply(ssr)) {
				result = RDFDataMgrEx.execConstruct(conn, sparqlFilenameOrUri);				
			}
			
			if(allowCacheWrite) {
				RDFDataMgr.write(new FileOutputStream(cacheFile.toFile()), result, RDFFormat.TURTLE);
			}
		}
		
		return result;
	}
}

interface ModelFile {
	Model load();
}

class RdfWorkflowSpec {
	protected Map<SparqlServiceReference, Function<SparqlServiceReference, RDFConnection>> rdfConnectionFactoryRegistory = new LinkedHashMap<>();
	
	public RdfWorkflowSpec registerConnectionFactory(SparqlServiceReference ssr, Function<SparqlServiceReference, RDFConnection> fn) {
		rdfConnectionFactoryRegistory.put(ssr, fn);
		return this;
	}
	
	public ModelCreation deriveDatasetWithSparql(SparqlServiceReference ssr, String filenameOrUri) {
		return new ModelCreationImpl(ssr, filenameOrUri, rdfConnectionFactoryRegistory);
	}
}

public class DatasetCache {
	public static void main(String[] args) throws Exception {
		// The workflow we want to implement is:
		// 1. Create a RDFConnection to a SPARQL endpoint
		// 2. Create a derived dataset by executing a sequence of SPARQL construct queries
		//    from .sparql file against it
		// 3. Cache the resulting dataset
		String serviceUrl = "http://localhost:8890/sparql";
		DatasetDescription dd = new DatasetDescription();
		dd.addDefaultGraphURI("http://dbpedia.org");
		
		SparqlServiceReference ssr = new SparqlServiceReference(serviceUrl, dd);
		
		Model model = new RdfWorkflowSpec()
			.registerConnectionFactory(ssr, x -> null)
			.deriveDatasetWithSparql(ssr, "analyze-numeric-properties.sparql")
			.cache()
			.getModel();
		
//		Model model = RdfWorkflowRunner
//			.loadModel(spec);
		
	}
}


