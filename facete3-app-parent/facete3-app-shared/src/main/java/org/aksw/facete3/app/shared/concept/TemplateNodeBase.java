package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.expr.Expr;

public abstract class TemplateNodeBase
    implements TemplateNode
{
    protected Expr idExpr;

    public TemplateNodeBase(Expr idExpr) {
        super();
        this.idExpr = idExpr;
    }

    @Override
    public Expr getIdExpr() {
        return idExpr;
    }

}
