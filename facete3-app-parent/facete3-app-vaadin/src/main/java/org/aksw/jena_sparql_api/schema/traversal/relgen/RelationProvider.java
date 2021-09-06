package org.aksw.jena_sparql_api.schema.traversal.relgen;

import org.aksw.jena_sparql_api.concepts.Relation;


/**
 * Simply a supplier of relations by name.
 *
 * @author raven
 *
 */
public interface RelationProvider {
    Relation getRelation(String relationId);
}
