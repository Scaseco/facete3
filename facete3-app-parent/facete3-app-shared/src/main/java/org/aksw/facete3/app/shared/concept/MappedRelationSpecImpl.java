package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.syntax.Template;

public class MappedRelationSpecImpl
    implements MappedRelationSpec
{
    protected RelationSpec relationSpec;
    protected Template template;

    public MappedRelationSpecImpl(RelationSpec relationSpec, Template template) {
        super();
        this.relationSpec = relationSpec;
        this.template = template;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public RelationSpec getRelationSpec() {
        return relationSpec;
    }
}

