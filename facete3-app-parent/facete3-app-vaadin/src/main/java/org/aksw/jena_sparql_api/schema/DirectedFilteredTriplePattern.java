package org.aksw.jena_sparql_api.schema;

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

    public DirectedFilteredTriplePattern(Triple triple, ExprList exprs, boolean isForward) {
        super();
        this.triplePattern = triple;
        this.exprs = exprs;
        this.isForward = isForward;
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

