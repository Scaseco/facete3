package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

public interface NfaTransition
	extends Resource
{
	@Iri("eg:from")
	NfaState getSource();
	
	@Iri("eg:to")
	NfaState getTarget();
	
	@Iri("eg:weight")
	Double getWeight();

	NfaTransition setSource(Resource source);
	NfaTransition setTarget(Resource target);
	Double setWeight(Double weight);
}
