package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.apache.jena.rdf.model.Resource;


public interface Nfa 
	extends Resource
{
	@IriNs("eg")
	NfaState getStartState();

	@Iri("eg:transition")
	Collection<NfaTransition> getTransitions();
}
