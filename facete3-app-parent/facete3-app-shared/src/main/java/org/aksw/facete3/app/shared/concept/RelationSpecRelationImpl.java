package org.aksw.facete3.app.shared.concept;

import org.aksw.jena_sparql_api.concepts.Relation;


public class RelationSpecRelationImpl
    extends RelationSpecBase
    implements RelationSpecRelation
{
    protected Relation relation;

    public RelationSpecRelationImpl(Relation relation) {
        super();
        this.relation = relation;
    }

    @Override
    public boolean isRelation() {
        return true;
    }

    @Override
    public Relation getRelation() {
        return relation;
    }

}
