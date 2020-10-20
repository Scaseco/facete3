package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.expr.Expr;

public interface TemplateNodeHolder
    extends TemplateNodeDelegate
{
    void setDelegate(TemplateNode delegate);

    default TemplateNode makeEnumerated() {
        Expr idExpr = getIdExpr();
        setDelegate(new TemplateNodeConcreteImpl(idExpr));
        return this;
    }

    default TemplateNode makeVariable() {
        Expr idExpr = getIdExpr();
        setDelegate(new TemplateNodeConcreteImpl(idExpr));
        return this;
    }
}
