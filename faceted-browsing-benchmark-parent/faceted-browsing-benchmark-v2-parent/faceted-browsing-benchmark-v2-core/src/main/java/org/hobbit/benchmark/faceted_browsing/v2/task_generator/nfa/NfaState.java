package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface NfaState
	extends Resource
{
	@Iri("eg:outgoingTransition")
	Collection<NfaTransition> getOutgoingTransitions();
	
	@Iri("eg:ingoingTransition")
	Collection<NfaTransition> getIngoingTransitions();
}
