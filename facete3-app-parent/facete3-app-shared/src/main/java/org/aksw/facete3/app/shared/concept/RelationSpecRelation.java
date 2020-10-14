package org.aksw.facete3.app.shared.concept;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.sparql.core.Var;

public interface RelationSpecRelation
    extends RelationSpec
{
    Relation getRelation();

    @Override
    default List<Var> getVars() {
        Relation relation = getRelation();
        List<Var> result = relation.getVars();
        return result;
    }
}
