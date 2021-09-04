package org.aksw.jena_sparql_api.schema.traversal.rdf;

import java.util.Set;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathNode;
import org.aksw.jena_sparql_api.schema.NodeSchema;

/**
 *
 *
 * @author raven
 *
 */
interface SchemaFileSystem {
    NodeSchema getRootNodeSchema();
}




/**
 * Traverse the SHNodeShape and SHPropertyShapes.
 * Same as Resource traversal?
 *
 *
 * @author raven
 *
 */
public interface SchemaTraversals {


    interface SetNode
    {
        //SchemaFileSystem getSchemaFileSystem();



    }


    public static SetNode from(NodeSchema rootNode) {
        return null;

    }

}
