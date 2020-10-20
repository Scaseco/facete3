package org.aksw.facete3.app.shared.concept;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

public class TemplateNodeConcreteImpl
    extends TemplateNodeBase
    implements TemplateNodeConcrete
{

    public TemplateNodeConcreteImpl(Expr idExpr) {
        super(idExpr);
    }

    @Override
    public Map<Node, TemplateNode> getMapping() {
        // TODO Auto-generated method stub
        return null;
    }
}
