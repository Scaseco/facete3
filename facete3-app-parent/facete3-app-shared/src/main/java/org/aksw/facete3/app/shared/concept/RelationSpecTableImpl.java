package org.aksw.facete3.app.shared.concept;

import org.apache.jena.sparql.algebra.Table;

public class RelationSpecTableImpl
    extends RelationSpecBase
    implements RelationSpecTable
{
    protected Table table;

    public RelationSpecTableImpl(Table table) {
        super();
        this.table = table;
    }

    @Override
    public boolean isTable() {
        return true;
    }

    @Override
    public Table getTable() {
        return table;
    }
}
