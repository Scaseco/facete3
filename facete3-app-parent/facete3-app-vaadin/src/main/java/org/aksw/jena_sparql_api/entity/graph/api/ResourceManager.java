package org.aksw.jena_sparql_api.entity.graph.api;

import org.aksw.jena_sparql_api.schema.NodeSchema;
import org.apache.jena.graph.Node;

public interface ResourceManager {




    void register(Node node, NodeSchema nodeSchema);


    /**
     * Update all resources managed by this manager with information
     * from either the cache or the backend */
    void sync();
}
