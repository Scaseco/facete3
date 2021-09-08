package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Set;

import org.aksw.jena_sparql_api.algebra.utils.FilteredQuad;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

import com.google.common.collect.Table;

public class ResourceLoadState {
    Node src;
    Table<Node, Node, Node> pog;

    Set<Node> ingoingProperties;
    Set<Node> outgoingProperties;

    public void getAccumulator(FilteredQuad fq) {
    	
    }

//    public getPartitionAccumulators(Quad quad) {
//
//    }
}
