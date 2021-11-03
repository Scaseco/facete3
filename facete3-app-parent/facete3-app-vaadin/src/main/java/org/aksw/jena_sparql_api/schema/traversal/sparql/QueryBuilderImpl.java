package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jenax.sparql.relation.api.Relation;

public class QueryBuilderImpl
    implements QueryBuilder
{
    protected Relation baseRelation;


    public QueryBuilderImpl(Relation baseRelation) {
        super();
        this.baseRelation = baseRelation;
    }


    @Override
    public Relation getBaseRelation() {
        return baseRelation;
    }


    @Override
    public String toString() {
        return "QueryBuilderImpl [baseRelation=" + baseRelation + "]";
    }

}
