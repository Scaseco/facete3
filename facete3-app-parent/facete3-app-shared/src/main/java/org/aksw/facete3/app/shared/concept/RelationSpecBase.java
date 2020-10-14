package org.aksw.facete3.app.shared.concept;

public abstract class RelationSpecBase
    implements RelationSpec
{
    @Override
    public boolean isTable() {
        return false;
    }

    @Override
    public RelationSpecTable asTable() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isRelation() {
        return false;
    }

    @Override
    public RelationSpecRelation asRelation() {
        throw new UnsupportedOperationException();
    }
}
