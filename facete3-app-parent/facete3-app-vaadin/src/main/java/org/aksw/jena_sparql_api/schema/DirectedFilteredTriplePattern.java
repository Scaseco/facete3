package org.aksw.jena_sparql_api.schema;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.expr.ExprList;

/**
 * A single triple pattern combined with a filter and a direction.
 *
 *
 * @author raven
 *
 */
public class DirectedFilteredTriplePattern {
    protected Triple triplePattern;
    protected ExprList exprs;

    /** If isForward is true then the subject acts as the source and the object as the target.
     * otherwise its vice versa.
     */
    protected boolean isForward;

    public DirectedFilteredTriplePattern(Triple triplePattern, ExprList exprs, boolean isForward) {
        super();
        this.triplePattern = triplePattern;
        this.exprs = exprs;
        this.isForward = isForward;
    }

    public static DirectedFilteredTriplePattern create(Node source, Node predicate, boolean isForward) {
        return new DirectedFilteredTriplePattern(Triple.create(source, predicate, Vars.o), null, isForward);
    }

    public Node getSource() {
        return TripleUtils.getSource(triplePattern, isForward);
    }

    public Node getTarget() {
        return TripleUtils.getTarget(triplePattern, isForward);
    }

    public Triple getTriplePattern() {
        return triplePattern;
    }

    public ExprList getExprs() {
        return exprs;
    }

    public boolean isForward() {
        return isForward;
    }
}

