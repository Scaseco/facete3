package org.aksw.vaadin.datashape.form;

import org.aksw.jena_sparql_api.collection.GraphChange;
import org.aksw.jena_sparql_api.collection.ObservableValue;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

public class MainEditorTest {

    public static void main(String[] args) {
        GraphChange graph = new GraphChange();



        Model model = ModelFactory.createModelForGraph(graph.getBaseGraph());
        Resource a = model.createResource("urn:a");
        Resource b = model.createResource("urn:b");
        Resource c = model.createResource("urn:c");

        a
            .addLiteral(RDFS.label, "a");
        b
            .addLiteral(RDFS.label, "b");
        c
            .addLiteral(RDFS.label, "c");

        graph.getRenamedNodes().put(b.asNode(), a.asNode());
        graph.getRenamedNodes().put(a.asNode(), b.asNode());

        Model inferredModel = ModelFactory.createModelForGraph(graph.getSameAsInferredGraphView());
        Model effectiveModel = ModelFactory.createModelForGraph(graph.getEffectiveGraphView());


        if (false) {
            System.out.println("Inferred Model");
            RDFDataMgr.write(System.out, inferredModel, RDFFormat.TURTLE_PRETTY);
        }

        if (true) {
            System.out.println("Effective Model");
            RDFDataMgr.write(System.out, effectiveModel, RDFFormat.TURTLE_PRETTY);

//            System.out.println("a:" + a.inModel(effectiveModel).listProperties().toList());
//            System.out.println("b:" + b.inModel(effectiveModel).listProperties().toList());
//            System.out.println("c:" + c.inModel(effectiveModel).listProperties().toList());

        }


        Triple t = a.getModel().listStatements(a, RDFS.label, "a").toList().get(0).asTriple();
        ObservableValue<Node> test = graph.createFieldForExistingTriple(t, 2);
        test.addListener(ev -> {
            System.out.println("Got event: " + ev);
        });
        System.out.println(test.get());
        test.set(NodeFactory.createLiteral("yay"));
        System.out.println(test.get());


//        System.out.println("b:" + b.inModel(viewModel).listProperties().toList());
    }

    public static void mainReasoning(String[] args) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
        Resource a = model.createResource("urn:a");
        Resource b = model.createResource("urn:b");

        a
            .addLiteral(RDFS.label, "a");
        b
            .addLiteral(RDFS.label, "b");

        a.addProperty(OWL.sameAs, b);
        for (Individual ind : model.listIndividuals().toList()) {
            ind.listProperties().forEachRemaining(System.out::println);
        }

//        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }
}
