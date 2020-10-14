package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.syntax.Template;

public interface MappedRelationSpec {
    Template getTemplate();
    RelationSpec getRelationSpec();
}
