package org.aksw.facete3.app.shared.concept;

import java.util.List;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;

public interface RelationSpecTable
    extends RelationSpec
{
    Table getTable();

    @Override
    default List<Var> getVars() {
        Table table = getTable();
        List<Var> result = table.getVars();
        return result;
    }
}

