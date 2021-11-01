package org.aksw.facete3.app.shared.concept;

import java.util.Arrays;

import org.aksw.commons.util.slot.Slot;
import org.aksw.commons.util.slot.SlottedBuilder;
import org.aksw.commons.util.slot.SlottedBuilderImpl;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.NodeFactory;

public class ConceptBuilderWithSlots
{




    public static void main(String[] args) {
        SlottedBuilder<UnaryRelation, UnaryRelation> builder = SlottedBuilderImpl.create(VirtualPartitionedQuery::unionUnary);

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
