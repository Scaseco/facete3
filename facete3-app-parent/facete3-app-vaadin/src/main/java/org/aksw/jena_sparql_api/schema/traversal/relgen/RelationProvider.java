package org.aksw.jena_sparql_api.schema.traversal.relgen;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathPE;


/**
 * A supplier that sees all constraints on prior relations plus
 * the 'block' of constraints on its predecessor.
 *
 * @author raven
 *
 */
public interface RelationProvider {
    Relation getRelation(PathPE absPath, PathPE block);
}
