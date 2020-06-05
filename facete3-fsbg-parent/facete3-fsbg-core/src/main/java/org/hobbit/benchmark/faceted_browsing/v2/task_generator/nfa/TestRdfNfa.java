package org.hobbit.benchmark.faceted_browsing.v2.task_generator.nfa;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rx.SparqlStmtMgr;
import org.apache.jena.ext.com.google.common.collect.Range;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class TestRdfNfa {
    public static void main(String[] args) {
        Model model = RDFDataMgr.loadModel("task-generator-config.ttl");
        SparqlStmtMgr.execSparql(model, "nfa-materialize.sparql");

        JenaPluginUtils.scan(Nfa.class);

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

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

        createConcreteNfa(new Random(), configs.iterator().next());
    }


    public static Nfa createConcreteNfa(Random rand, Nfa nfa) {
        // Go through each transition and replace its weight range with a concrete value

        Collection<NfaTransition> transitions = nfa.getTransitions();
        for(NfaTransition transition : transitions) {
            // Check all related objects for whether they carry min / max attributes

            Set<Statement> stmts = transition.listProperties()
                .filterKeep(s -> s.getObject().isResource())
                .toSet();

            for(Statement stmt : stmts) {
                Resource s = stmt.getSubject();
                Property p = stmt.getPredicate();
                Resource o = stmt.getObject().asResource();
                Number min = ResourceUtils.getLiteralPropertyValue(o, Vocab.min, Number.class);
                Number max = ResourceUtils.getLiteralPropertyValue(o, Vocab.max, Number.class);

                if(min == null && max == null) {
                    // Nothing todo / skip
                } else if(min != null && max != null) {
                    Range<Double> range = Range.closedOpen(min.doubleValue(), max.doubleValue());
//					Range<Double> r = x.getValue().intersection(Range.closedOpen(0.0, 1.0));
                    double point = range.lowerEndpoint() + rand.nextDouble() * (range.upperEndpoint() - range.lowerEndpoint());

                    s.removeAll(p);
                    o.removeProperties();
                    s.addLiteral(p, point);

                } else {
                    throw new RuntimeException("Ranges must be restricted on both ends; " + o + "min: " + min + ", max: " + max);
                }

                System.out.println("min: " + min + ", max: " + max);
            }
        }

        RDFDataMgr.write(System.out, nfa.getModel(), RDFFormat.TURTLE_PRETTY);

        //System.out.println(states);

        return nfa;
    }
}
