package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	
	default Collection<NfaState> getStates() {
		Collection<NfaTransition> transitions = getTransitions();
		Set<NfaState> result = transitions.stream()
				.flatMap(t -> Stream.of(t.getSource(), t.getTarget()))
				.distinct()
				.collect(Collectors.toSet());
				
		return result;
	}
}
