package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.PolaritySet;
import org.apache.jena.ext.com.google.common.math.LongMath;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class ResourceLoader {
    /** A dataset with the metamodel */
    protected Dataset resourceMetamodelDataset;

    /** The connection to the data */
    protected SparqlQueryConnection conn;


    long threshold = 100;

    public void load() {
        // resourceMetamodelDataset.get(resource);

        Node node = null;
        ResourceGraphMetamodel metamodel = getMetamodel(node);

        for (int i = 0; i < 2; ++i) {
            boolean isFwd = i == 0;

            Set<Node> blacklist = new HashSet<>();

            for (Entry<Node, PredicateStats> pStats : metamodel.getPredicateStats(isFwd).entrySet()) {
                Node p = pStats.getKey();

                long count = 0;
                for (Entry<Node, GraphPredicateStats> gpStats : pStats.getValue().getGraphToPredicateStats().entrySet()) {
                    Long contrib = gpStats.getValue().getDistinctValueCount();
                    if (contrib == null) {
                        contrib = Long.MAX_VALUE;
                    }

                    count = LongMath.saturatedAdd(count, contrib);
                }

                if (count >= threshold) {
                    blacklist.add(p);
                }
            }
        }

        // Blacklist all predicates with more than $threshold values
        PolaritySet<Node> blacklist = new PolaritySet<>();





    }

}
