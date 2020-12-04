package org.aksw.jena_sparql_api.collection;

import org.aksw.facete3.app.vaadin.components.rdf.editor.TripleConstraint;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

/**
 *
 *
 * @author raven
 *
 */
public class GraphWithFilter
    extends GraphWrapper
    implements Graph
{
    protected Graph delegate;
    protected TripleConstraint predicate;

    public GraphWithFilter(Graph graph, TripleConstraint predicate) {
        super(graph);
        this.predicate = predicate;
    }

    @Override
    public void add(Triple t) /* throws AddDeniedException */ {
        boolean isAccepted = predicate.test(t);
        if (isAccepted) {
            super.add(t);
        }
    }

//    @Override
//    public void remove(Node s, Node p, Node o) {
//
//    	super.remove(s, p, o);
//    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        boolean isAccepted = predicate.test(new Triple(s, p, o));
        boolean result = isAccepted && super.contains(s, p, o);

        return result;
    }


    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Triple a = predicate.getMatchTriple();
        Triple b = Triple.createMatch(s, p, o);
        Triple combinedPattern = TripleUtils.logicalAnd(a, b);

        ExtendedIterator<Triple> result = combinedPattern == null
                ? NiceIterator.emptyIterator()
                : super.find(combinedPattern).filterKeep(predicate::test);

        return result;
    }

}