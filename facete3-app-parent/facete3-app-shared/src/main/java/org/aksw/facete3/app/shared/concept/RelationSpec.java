package org.aksw.facete3.app.shared.concept;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * A class that unifies intensional and extensional representations of a result set.
 * The intensional case is covered by a sub-class that holds a SPARQL relation
 * which is a SPARQL graph pattern + a list of variables thereof.
 *
 * The extensional representation may use {@link Binding} or even {@link QuerySolution}.
 *
 *
 * @author raven
 *
 */
public interface RelationSpec {

    List<Var> getVars();

    boolean isTable();
    RelationSpecTable asTable();

    boolean isRelation();
    RelationSpecRelation asRelation();

    default Table getTable() {
        return asTable().getTable();
    }

    default Relation getRelation() {
        return asRelation().getRelation();
    }
}
