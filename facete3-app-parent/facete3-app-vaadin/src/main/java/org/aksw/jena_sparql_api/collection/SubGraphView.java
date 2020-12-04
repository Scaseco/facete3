package org.aksw.jena_sparql_api.collection;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.core.UpdateContext;
import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.SetFromGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.GraphWrapper;




class RelatedGraph {

    ObservableSet<Node> nodeSet;

    /** The node schema which to apply to each item in order to create
     * a SubGraph view. */
    NodeSchema nodeSchema;



    // The (initial) subgraph view which to apply for each item
    SubGraphView subGraphView;
}

//class RelatedGraphBuilder {
//	setProperty(Node);
//}


class SubGraphSpec {
    protected Expr outFilter;
    protected Expr inFilter;
}

class SubGraphSet
//    extends Collection<SubGraphView>
{
    protected SetFromGraph items;
    protected SubGraphSpec subGraphSpec;

    public SubGraphSet(ObservableGraphImpl baseGraph) {
        // baseGraph.getEventManager()

    }

    /** The set of items for which the sub graph specifies their related triples  */
    protected SetFromGraph getRawItemSet() {
        return items;
    }

    SubGraphSpec getSubGraphSpec;

}

/**
 * - Definition of a set of nodes based on a filter + map operation on the set of triples
 *
 *
 *
 * @author raven
 *
 */
public class SubGraphView
    extends GraphWrapper
{
    /** An expression making use at most of the variables ?s ?p and ?o */
    protected Expr tripleFilterExpr;


//    public RelatedGraphBuilder relatedGraphBuilder() {
//        return null;
//    }

    public ObservableSet<Triple> createSubSet(Expr expr) {
        return null;
//    	return new SetFromGraph(this);
    }




    // protected SetFromGraph relatedNodes;


    public SubGraphView(Graph graph) {
        super(graph);
    }

}
