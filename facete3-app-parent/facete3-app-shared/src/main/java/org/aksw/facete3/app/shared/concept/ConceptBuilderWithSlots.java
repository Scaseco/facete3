package org.aksw.facete3.app.shared.concept;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.facete3.app.shared.concept.SlottedBuilder.Slot;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;

public class ConceptBuilderWithSlots
{

    /**
     * Create a union from a given collection of unary relations
     * If the collection is empty, return a relation with an empty (unit) table.
     *
     * TODO Move to ConceptUtils
     *
     * @param relations
     * @return
     */
    public static UnaryRelation union(Collection<? extends UnaryRelation> relations) {
//        Set<Var> mentionedVars = relations.stream()
//                .map(Relation::getVarsMentioned)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toSet());
        UnaryRelation result;

        if (relations.isEmpty()) {
            result = new Concept(new ElementData(), Vars.x);
        } else {

            UnaryRelation first = relations.iterator().next();
            Var tgtVar = first.getVar();
            List<Var> tgtVars = Collections.singletonList(tgtVar);

            List<Element> elements = relations.stream()
                    .map(r -> RelationUtils.rename(r, tgtVars))
                    .map(Relation::toUnaryRelation)
                    .map(Relation::getElement)
                    .collect(Collectors.toList());

            Element e = ElementUtils.unionIfNeeded(elements);

            result = new Concept(e, tgtVar);
        }
        return result;
    }


    public static void main(String[] args) {
        SlottedBuilder<UnaryRelation, UnaryRelation> builder = SlottedBuilderImpl.create(ConceptBuilderWithSlots::union);

        Slot<UnaryRelation> slot1 = builder.newSlot();
        slot1.set(ConceptUtils.createFilterConcept(Arrays.asList(
                NodeFactory.createURI("http://sl.ot/1")
            )));

        Slot<UnaryRelation> slot2 = builder.newSlot();
        slot2.set(ConceptUtils.createSubjectConcept());

        // TODO Also ensure variables in ElementData get property renamed
        Slot<UnaryRelation> slot3 = builder.newSlot();
        slot3.set(ConceptUtils.createFilterConcept(Arrays.asList(
                NodeFactory.createURI("http://sl.ot/3")
            )));

        UnaryRelation whole;
        System.out.println(whole = builder.build());

        slot2.close();
        System.out.println(whole = builder.build());

    }
}
