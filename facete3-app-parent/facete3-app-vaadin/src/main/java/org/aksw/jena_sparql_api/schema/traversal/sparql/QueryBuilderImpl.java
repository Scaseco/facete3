package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jenax.sparql.fragment.api.Fragment;

public class QueryBuilderImpl
    implements QueryBuilder
{
    protected Fragment baseRelation;


    public QueryBuilderImpl(Fragment baseRelation) {
        super();
        this.baseRelation = baseRelation;
    }


    @Override
    public Fragment getBaseRelation() {
        return baseRelation;
    }


    @Override
    public String toString() {
        return "QueryBuilderImpl [baseRelation=" + baseRelation + "]";
    }

}
