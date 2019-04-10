package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.RDFConnectionEx;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.utils.RDFDataMgrRx;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.util.ResourceUtils;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingEncoders;
import org.hobbit.core.component.BenchmarkVocab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ObjectArrays;

import io.reactivex.Flowable;

public class MainSparqlBenchmarkRunner {
	private static final Logger logger = LoggerFactory.getLogger(MainSparqlBenchmarkRunner.class);
	
	
	public static void main(String[] args) throws Exception {
		// HACK/WORKAROUND for Jcommander issue
		// https://github.com/cbeust/jcommander/issues/464
		// Add a dummy element to initialize a list property
		args = ObjectArrays.concat(new String[] {"-d", "foo"}, args, String.class);

		
		JenaPluginUtils.registerJenaResourceClasses(
			CommandMain.class
		);
		
		CommandMain cmMain = ModelFactory.createDefaultModel().createResource().as(CommandMain.class);

		JCommander jc = JCommander.newBuilder()
				.addObject(cmMain)
				.build();

		jc.parse(args);
		
		if(cmMain.getHelp()) {
			jc.usage();
			return;
		}

		
		DatasetDescription datasetDescription = new DatasetDescription();
		datasetDescription.addAllDefaultGraphURIs(cmMain.getDefaultGraphUris());

		List<String> nonOptionArgs = cmMain.getNonOptionArgs();
		if(nonOptionArgs.size() != 1) {
			throw new RuntimeException("expected exactly one non-option argument with the benchmark file in .trig format");
		}
		
		
		String sparqEndpoint = cmMain.getSparqlEndpoint();

		RDFConnectionEx conn = RDFConnectionFactoryEx.connect(sparqEndpoint, datasetDescription);

		
		String filename = nonOptionArgs.get(0);
		String tag = cmMain.getTag();
		
		String suffix = tag == null ? "" : "-" + tag;
		
		Flowable<Dataset> taskFlow = RDFDataMgrRx
			.createFlowableResources(() -> new FileInputStream(filename), Lang.TRIG, "http://www.example.org/")
			.map(r -> r.as(SparqlTaskResource.class))
			.map(task -> {
				Model result = ModelFactory.createDefaultModel();
				Resource r = task.inModel(result.add(ResourceUtils.reachableClosure(task)));

				logger.info("Processing " + r);
				//String str = task.getSparqlStmtString();
				SparqlStmt stmt = SparqlTaskResource.parse(task);

				r = ResourceUtils.renameResource(r, r.getURI() + suffix);
				r.addProperty(ResourceFactory.createProperty("http://www.example.org/origin"), task);
				
				
								
				Stopwatch sw = Stopwatch.createStarted();
//				SPARQLResultEx ex = SparqlStmtUtils.execAny(conn, stmt);
//				ex.close();

				ResultSetMem rsMem;
				int numRows;
				String queryStr = stmt.getAsQueryStmt().getOriginalString();
				try(QueryExecution qe = conn.query(queryStr)) {
		        	ResultSet resultSet = qe.execSelect();
		        	//int wtf = ResultSetFormatter.consume(resultSet);
		        	rsMem = new ResultSetMem(resultSet);
		        	numRows = ResultSetFormatter.consume(rsMem);
		            logger.info("Number of expected result set rows for task " + task + ": " + numRows + " query: " + queryStr);
		        }
				
				long elapsed = sw.stop().elapsed(TimeUnit.MILLISECONDS);

				r.addLiteral(ResourceFactory.createProperty("http://www.example.org/elapsed"), elapsed);

				// Add actual result
	        	rsMem.rewind();
	        	String resultSetStr = FacetedBrowsingEncoders.resultSetToJsonStr(rsMem);
	        	r
	        		.addLiteral(BenchmarkVocab.actualResult, resultSetStr)
	        		.addLiteral(BenchmarkVocab.actualResultSize, numRows);

				logger.info("Processed " + r + " - elapsed: " + elapsed);

				Dataset x = DatasetFactory.create();
				x.addNamedModel(r.getURI(), result);
				return x;
			});

		RDFDataMgrRx.writeDatasets(taskFlow, System.out, RDFFormat.TRIG);
	}
}
