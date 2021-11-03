package org.aksw.jena_sparql_api.entity.graph.api;

import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplate;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;

import com.google.common.collect.Multimap;

/**
 * An entity graph fragment corresponds to a set of triple patterns with a designated root.
 * The predicates of these triple patterns may by variables.
 * A couple comprised of a sparql relation pattern and a
 * corresponding entity template that maps the bindings to triples.
 * The entity template must designate exactly one node as the root node.
 *
 * ENTITY ?root
 * CONSTRUCT {
 *   ?root :prop ?value ;
 *         ?p ?o
 * WHERE { ?root:foo ?value ; ?p ?o }
 *
 * @author raven
 *
 */
public interface EntityGraphFragment {

    /**
     * An EntityGraphFragment is owned by a single EntityGraphNode.
     * The construction of the effective relation may need to access the owner's
     * base relation such as in order to compute aggregated values from it.
     *
     * @return
     */
    EntityGraphNode getOwner();

    /**
     * The effective relation pattern contributed by the entity graph fragment.
     * May inject or reference the owner's base relation pattern.
     *
     * References are expressed by exploiting the SPARQL service clause to act as a slot
     * for graph pattern injection:
     *
     * SELECET ?entityKey (COUNT(*) AS ?c) {
     *   SERVICE <slot:owner> {}
     *
     * @return
     */
    Relation getEffectiveRelation();

    /**
     * The entity template. This is similar to a construct querie's template with the
     * addition that one or more nodes can be designated as starting points for
     * conceptually traversing the graph spanned by an entity's attributes.
     *
     * @return
     */
    EntityTemplate getEntityTemplate();

    /**
     * Any variable or blank node in the entity template may be mapped to
     * a collection of EntityGraphNodes which can effectively be used to expand the
     * entity graph.
     *
     * @return
     */
    Multimap<Node, EntityGraphNode> getTargetEntityGraphNodeMap();
}