package org.aksw.facete3.app.vaadin.components.rdf.editor;

import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.expr.Expr;

/**
 * A predicate that can be evaluated over individual triples.
 * The sparql expression allows obtaining the set of matching triples
 * for any a local or remote RDF graph.
 *
 */
public interface TripleConstraint
    extends Predicate<Triple>
{
    /**
     * A possibly (less selective) representation of the constraint as a match triple suitable for
     * pre-filtering using {@link Graph#find(Triple)}
     *
     * Must never return null. The most unselective triple pattern
     * is Triple.createMatch(null, null, null).
     *
     * @return
     */
    Triple getMatchTriple();

    /**
     * True if {@link #getMatchTriple()} matchs the same set of triples as {@link #getExpr()}.
     */
    boolean isMatchTripleExhaustive();

    /** An expression which only allows a subset of the variables ?s ?p and ?o */
    Expr getExpr();
}
