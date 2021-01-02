package org.aksw.vaadin.datashape.form;

import org.aksw.facete3.app.vaadin.components.rdf.editor.TripleConstraintImpl;
import org.aksw.jena_sparql_api.collection.ObservableCollection;
import org.aksw.jena_sparql_api.collection.ObservableGraph;
import org.aksw.jena_sparql_api.collection.ObservableGraphImpl;
import org.aksw.jena_sparql_api.collection.ObservableSet;
import org.aksw.jena_sparql_api.collection.ObservableSetFromGraph;
import org.aksw.jena_sparql_api.collection.ObservableSubGraph;
import org.aksw.jena_sparql_api.collection.ObservableValue;
import org.aksw.jena_sparql_api.collection.ObservableValueFromObservableCollection;
import org.aksw.jena_sparql_api.collection.SetOfNodesFromGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class TestObservableGraph {
    public static void main(String[] args) {
        ObservableGraphImpl graph = new ObservableGraphImpl(GraphFactory.createDefaultGraph());

        //  t -> t.getSubject().matches(RDF.Nodes.first
        ObservableGraph subGraph = ObservableSubGraph.decorate(graph,
                TripleConstraintImpl.create(RDF.Nodes.first, null, null));

        subGraph.addPropertyChangeListener(ev -> System.out.println("Got subgraph event: " + ev));

        ObservableCollection<Node> test = SetOfNodesFromGraph.create(graph, RDF.Nodes.first, RDFS.Nodes.label, true);
        test.addPropertyChangeListener(ev -> System.out.println("Set view: " + ev));


        // , RDF.Nodes.first, RDFS.Nodes.label, true
        ObservableSet<Triple> set = new ObservableSetFromGraph(subGraph);

        ObservableValue<Triple> value = ObservableValueFromObservableCollection.decorate(set);
        value.addPropertyChangeListener(ev -> {
            System.out.println("Value changed: " + ev);
        });

        set.addPropertyChangeListener(event -> {
            System.out.println("Triple event: " + event);
        });


        graph.add(new Triple(RDF.Nodes.type, RDFS.Nodes.label, NodeFactory.createLiteral("type")));

        graph.add(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("Hello")));
        graph.add(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("World")));
        graph.delete(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("World")));
        graph.delete(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("World")));

        graph.add(new Triple(RDF.Nodes.first, RDFS.Nodes.label, NodeFactory.createLiteral("Cheers")));

        graph.add(new Triple(RDF.Nodes.rest, RDFS.Nodes.label, NodeFactory.createLiteral("Hello")));


        System.out.println("Items: " + test);
        subGraph.clear();
        System.out.println("Items: " + test);

        graph.find().forEachRemaining(t -> System.out.println("remaining triple: " + t));
    }

}
