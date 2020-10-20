package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.expr.Expr;


public interface TemplateNode {
    Expr getIdExpr();

//    TemplateDirNode getFwd();
//    TemplateDirNode getBwd();

    default boolean isPredicateEnumeration() {
        return this instanceof TemplateNodeConcrete;
    }

    default boolean isPredicateVar() {
        return this instanceof TemplateNodeVar;
    }

    default TemplateNodeConcrete asEnumeration() {
        return (TemplateNodeConcrete)this;
    }

    default TemplateNodeVar asVar() {
        return (TemplateNodeVar)this;
    }
}

