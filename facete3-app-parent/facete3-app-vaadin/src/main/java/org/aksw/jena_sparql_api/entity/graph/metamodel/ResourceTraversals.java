package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.nio.file.Paths;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathNode;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathOpsNode;
import org.aksw.jena_sparql_api.schema.traversal.sparql.QueryBuilder;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravProviderTriple;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravProviderTripleSparql;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.NodeValue;
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


        System.out.println( Paths.get("/tmp").relativize(Paths.get("/tmp/foo")) );
        System.out.println( Paths.get("/tmp/foo").relativize(Paths.get("/tmp")) );

        PathNode r = PathOpsNode.get().newRoot();

        PathNode path = r.resolve(RDF.type).resolve(RDF.first).resolve(NodeValue.makeInteger(1).asNode());

        System.out.println(path);

        Path<Node> rel = r.relativize(path);
        String relStr = rel.toString();
        System.out.println(relStr);
        path = r.resolve(relStr);
        System.out.println("Recovered from string: " + path);

        System.out.println(path.relativize(r));

        // String str = r.toString();


        path = path.resolve(PathOpsNode.PARENT).normalize();
        System.out.println(path);

        UnaryRelation ur = Concept.parse("?s { ?s a <urn:Person> }");
        TravProviderTriple<QueryBuilder> provider = new TravProviderTripleSparql(ur);

        TravValues<QueryBuilder> root = provider.root();

        System.out.println(root.getValue());

        System.out.println(root.traverse(RDF.first.asNode()).getValue());
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
