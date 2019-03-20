package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import java.util.Set;

import org.aksw.jena_sparql_api.core.utils.RDFDataMgrEx;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class TestRdfNfa {
	public static void main(String[] args) {
		Model model = RDFDataMgr.loadModel("task-generator-config.ttl");
		RDFDataMgrEx.execSparql(model, "nfa-materialize.sparql");

		RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
		
		JenaPluginUtils.registerJenaResourceClassesUsingPackageScan(Nfa.class);
		
		Set<Nfa> configs = model.listResourcesWithProperty(RDF.type, Vocab.Nfa)
				.mapWith(r -> r.as(Nfa.class))
				.toSet();
		
		for(Nfa config : configs) {
			NfaState startState = config.getStartState();
			System.out.println(startState);
			System.out.println(startState.getOutgoingTransitions().size());
			for(NfaTransition transition : config.getTransitions()) {
//				System.out.println(config.getTransitions());
				System.out.println(transition.getSource());
			}
		}
	}
}
