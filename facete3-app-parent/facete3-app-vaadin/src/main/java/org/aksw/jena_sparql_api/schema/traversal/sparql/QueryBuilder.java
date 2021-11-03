package org.aksw.jena_sparql_api.schema.traversal.sparql;

import org.aksw.jenax.sparql.relation.api.Relation;

public interface QueryBuilder {
    Relation getBaseRelation();
}
