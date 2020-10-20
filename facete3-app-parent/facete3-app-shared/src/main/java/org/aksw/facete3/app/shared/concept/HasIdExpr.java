package org.aksw.facete3.app.shared.concept;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;

public interface HasIdExpr {
    /**
     * An expression that can computes an 'id' (of type {@link Node}) from a given set of {@link Binding}s.
     *
     * @return
     */
    Expr getIdExpr();

    /**
     * Test whether the expression is a single variable
     *
     * @return
     */
    default boolean isIdVar() {
        Expr expr = getIdExpr();
        boolean result = expr instanceof ExprVar;
        return result;
    }

    default Var getIdvar() {
        Expr expr = getIdExpr();
        Var result = expr.asVar();
        return result;
    }
}

