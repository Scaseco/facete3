package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;

public class MainGenerateKPILabels {
	public static void main(String[] args) throws URISyntaxException, IOException {
		Path kpi = Paths.get(MainGenerateKPILabels.class.getClassLoader().getResource("faceted-benchmark-kpis.txt").toURI());
		//Path rdf = Paths.get(MainGenerateKPILabels.class.getResource("faceted-benchmark-kpis.txt").toURI());
		
		Model model = RDFDataMgr.loadModel("benchmark.ttl");
		
		PrefixMapping pm = model;
		
		Files.lines(kpi)
			.map(String::trim)
			.filter(line -> !line.isEmpty())
			.map(pm::expandPrefix)
			.forEach(line -> {
				Resource r = model.createResource(line);
				System.out.println(r.getURI() +
						"\n    " + Optional.ofNullable(r.getProperty(RDFS.label)).map(Statement::getString).orElse("-") +
						"\n    " + Optional.ofNullable(r.getProperty(RDFS.comment)).map(Statement::getString).orElse("-"));
				System.out.println();
			});
	}
}
