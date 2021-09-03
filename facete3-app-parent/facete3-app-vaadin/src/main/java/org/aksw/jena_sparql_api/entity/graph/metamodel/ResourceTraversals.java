package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.List;

import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathOpsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;

import io.reactivex.rxjava3.core.Flowable;

public class ResourceTraversals {

    // Somehow we need a java.nio.FileSystem / Path architecture for
    // traversing RDF
    // The main complexity is that internally we need to deal with different
    // kinds of data providers.

    interface DataProvider {

    }

    interface DataQuerySpec {

    }

    interface DataNode {
        DataProvider getDataProvider();


        Flowable<? extends RDFNode> fetch(DataQuerySpec spec);

        // Shorthand for 'fetch all'
        default Flowable<? extends RDFNode> fetch() { return fetch(null); }
        default Flowable<Node> fetchNodes() { return fetch().map(RDFNode::asNode); }
    }


    // Base class for all ResourceTraversalNodes
    // Value, Direction, Property, Alias
    interface ResourceTraversalNode
        extends DataNode
    {
        // Get the path that corresponds to the traversal
        Path<Node> getPath();
        Object getNodeType();
    }

    interface ResourceNode
        extends ResourceTraversalNode
    {
        DirectionNode fwd();
        DirectionNode bwd();
    }


    interface DirectionNode
        extends ResourceTraversalNode
    {
        AliasNode via(Node predicate);
    }

    interface AliasNode
        extends ResourceTraversalNode
    {
        SetNode viaAlias(String alias);
    }

    interface SetNode
        extends ResourceTraversalNode
    {
        ResourceNode to(Node targetNode);
    }


    public static void main(String[] args) {

        Path<Node> r = PathOpsNode.get().newRoot();

        Path<Node> path = r.resolve(RDF.Nodes.type).resolve(RDF.Nodes.first);
        System.out.println(path);

        path = path.resolve(PathOpsNode.PARENT).normalize();
        System.out.println(path);

    }

    public static void exampleDraft() {

        SetNode root = null;

        Node charlie = null;

        List<Node> people = root.fetchNodes().toList().blockingGet(); // { <Anne> <Bob> }
        Node anne = people.get(0);

        root.to(anne).fetchNodes().toList().blockingGet(); // { fwd, bwd }


        root.to(anne).fwd().via(RDF.Nodes.type).viaAlias(null).to(charlie); // { charlie (in the context of that path) }


    }
}
