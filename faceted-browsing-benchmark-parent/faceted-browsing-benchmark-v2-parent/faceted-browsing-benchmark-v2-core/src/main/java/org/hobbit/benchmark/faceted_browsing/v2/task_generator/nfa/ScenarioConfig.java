package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.apache.jena.rdf.model.Resource;


public interface ScenarioConfig
	extends Resource
{
	@IriNs("eg")
	Integer getRandomSeed();
	
	@IriNs("eg")
//	RdfRange getScenarioLength();
	Integer getScenarioLength();
	
	@IriNs("eg")
	Nfa getNfa();

	ScenarioConfig setRandomSeed(Integer randomSeed);
//	ScenarioConfig setScenarioLength(Resource range);
	ScenarioConfig setScenarioLength(Integer scenarioLength);	
	ScenarioConfig setNfa(Resource nfa);
}
