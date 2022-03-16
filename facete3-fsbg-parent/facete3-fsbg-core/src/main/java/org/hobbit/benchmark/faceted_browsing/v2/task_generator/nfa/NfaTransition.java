package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import org.aksw.jenax.annotation.reprogen.Iri;
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

    @Iri("eg:preventUndo")
    Boolean preventUndo();

    NfaTransition setSource(Resource source);
    NfaTransition setTarget(Resource target);
    Double setWeight(Double weight);
}
