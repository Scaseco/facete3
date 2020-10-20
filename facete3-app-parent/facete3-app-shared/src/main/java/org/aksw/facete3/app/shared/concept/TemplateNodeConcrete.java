package org.aksw.facete3.app.shared.concept;

import java.util.Map;

import org.apache.jena.graph.Node;

public interface TemplateNodeConcrete
    extends TemplateNode
{
    Map<Node, TemplateNode> getMapping();
}
