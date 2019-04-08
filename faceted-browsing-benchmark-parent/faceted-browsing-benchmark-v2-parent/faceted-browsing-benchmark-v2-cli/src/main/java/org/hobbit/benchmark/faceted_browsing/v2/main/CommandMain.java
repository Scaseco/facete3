package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.apache.jena.rdf.model.Resource;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

// Parameters (sketch)
// What dataset to use (may be sparql endpoint)
// Streaming - what constitutes a streaming record, what defines their order, how many events to use
//   what (time) range
// Which local triple store to use (for indexing the data)
// Task generation
// Distribution of chokepoints
// Maximum number of scenarios
// Maximum number of queries

@Parameters(separators="=", commandDescription="Parameters")
public interface CommandMain
		extends Resource
{	
	@Iri("eg:nonOptionArg")
	List<String> getNonOptionArgs();
	
	@Iri("eg:sparqlEndpoint")
	String getSparqlEndpoint();

	@Iri("eg:defaultGraphUris")
	@IriType
	List<String> getDefaultGraphUris();
	
	@Iri("eg:pathFindingDataSummary")
	@IriType
	String getPathFindingDataSummaryUri();
	
	@Iri("eg:help")
	boolean getHelp();// =# false;
			
	
	@Parameter(names={"-h", "--help"}, help=true)
	CommandMain setHelp(boolean help);
	
	@Parameter(description = "Non option args")
	CommandMain setNonOptionArgs(List<String> args);

	@Parameter(names="-e", description="SPARQL Endpoint")
	CommandMain setSparqlEndpoint(String sparqlEndpoint);

	@Parameter(names="-d", description="Default Graph URIs")
	CommandMain setDefaultGraphUris(List<String> args);
	

	@Parameter(names="-s", description="Data Summary")
	CommandMain setPathFindingDataSummaryUri(String file);

//		@Parameter(names={"--r"}, description="Fraction of events to read from input - e.g . 0.5 for half of it")
//		public Long eventsRatio = null;
//
//		@Parameter(names={"--e"}, description="Number of (e)vents to read from input")
//		public Long numEvents = null;
//		
//		@Parameter(names = "--ns", description="Number of scenarios to generate")
//		public Long numScenarios = 10l;
//		
//		@Parameter(names={"--maxScenarioLength"}, description="Maximum length of a scenario")
//		public Long maxScenarioLength = 10l;
}