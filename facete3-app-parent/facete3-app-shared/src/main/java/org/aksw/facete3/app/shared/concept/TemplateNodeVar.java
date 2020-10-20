package org.aksw.facete3.app.shared.concept;

public interface TemplateNodeVar
    extends TemplateNode
{
    public boolean isFoward();
    TemplateNode getPredicateNode();
    TemplateNode getObjectNode();

}
