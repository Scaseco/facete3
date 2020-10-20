package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.expr.Expr;

public class TemplateNodeVarImpl
    extends TemplateNodeBase
    implements TemplateNodeVar
{
    public TemplateNodeVarImpl(
            Expr idExpr,
            boolean isFoward,
            TemplateNode predicateNode,
            TemplateNode objectNode) {
        super(idExpr);
        this.isFoward = isFoward;
        this.predicateNode = predicateNode;
        this.objectNode = objectNode;
    }

    protected boolean isFoward;
    protected TemplateNode predicateNode;
    protected TemplateNode objectNode;

    @Override
    public TemplateNode getPredicateNode() {
        return predicateNode;
    }

    @Override
    public TemplateNode getObjectNode() {
        return objectNode;
    }

    @Override
    public boolean isFoward() {
        return isFoward;
    }
}
