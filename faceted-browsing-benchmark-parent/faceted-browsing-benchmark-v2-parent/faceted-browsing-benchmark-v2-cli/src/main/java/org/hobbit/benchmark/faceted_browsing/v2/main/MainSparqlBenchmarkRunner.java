package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.RDFConnectionEx;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.core.utils.RDFDataMgrRx;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.util.ResourceUtils;

import com.beust.jcommander.JCommander;
import com.google.common.base.Stopwatch;

import io.reactivex.Flowable;

public class MainSparqlBenchmarkRunner {
	public static void main(String[] args) throws Exception {
		
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

				//String str = task.getSparqlStmtString();
				SparqlStmt stmt = SparqlTaskResource.parse(task);

				r = ResourceUtils.renameResource(r, r.getURI() + "-" + suffix);
				r.addProperty(ResourceFactory.createProperty("http://www.example.org/origin"), task);
				
				
								
				Stopwatch sw = Stopwatch.createStarted();
				SparqlStmtUtils.execAny(conn, stmt);

				long elapsed = sw.stop().elapsed(TimeUnit.MILLISECONDS);
				r.addLiteral(ResourceFactory.createProperty("http://www.example.org/elapsed"), elapsed);
				
				Dataset x = DatasetFactory.create();
				x.addNamedModel(r.getURI(), result);
				return x;
			});

		RDFDataMgrRx.writeDatasets(taskFlow, System.out, RDFFormat.TRIG);
	}
}
