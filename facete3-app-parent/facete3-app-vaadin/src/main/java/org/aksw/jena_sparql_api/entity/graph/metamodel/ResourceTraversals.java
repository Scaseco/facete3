package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.Path;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathNode;
import org.aksw.jena_sparql_api.entity.graph.metamodel.path.node.PathOpsNode;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
import org.aksw.jena_sparql_api.schema.traversal.api.Trav;
import org.aksw.jena_sparql_api.schema.traversal.sparql.QueryBuilder;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravProviderTriple;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravProviderTripleImpl;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravAlias;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravDirection;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravProperty;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTripleStateComputer;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravTripleVisitor;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleViews.TravValues;
import org.aksw.jena_sparql_api.schema.traversal.sparql.TravTripleVisitorSparql;
import org.aksw.jena_sparql_api.schema.traversal.sparql.different.Traversals5.Traversal5A;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l3.Trav3.Trav3A;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l3.Trav3.Trav3B;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l3.Trav3.Trav3C;
import org.aksw.jena_sparql_api.schema.traversal.sparql.l3.Trav3Provider;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.vocabulary.SH;

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


    public static void fun(String[] args) {
        Traversal5A<Node, Object, Boolean, Byte, Character, Short, Integer> test = null;

        // fun.traverse(Node.ANY).
        test.traverse(Node.ANY).traverse(null).parent().state();

    }


    public static void main(String[] args) {
        JenaSystem.init();
        SHFactory.ensureInited();

        JenaPluginUtils.registerResourceClasses(
                NodeSchemaFromNodeShape.class,
                PropertySchemaFromPropertyShape.class,
                DatasetMetamodel.class,
                ClassMetamodel.class,
                PredicateStats.class,
                GraphPredicateStats.class,

                ClassRelationModel.class,
                ClassMetamodel.class
                );


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
        // TravProviderTriple<QueryBuilder> provider = new TravProviderTripleSparql(ur);

        TravProviderTriple<Void> provider = TravProviderTripleImpl.create();
        // TravValues<QueryBuilder, ?> root = provider.root();

        TravTripleVisitor<QueryBuilder> gen = TravTripleVisitorSparql.create(ur);

        TravValues<Void> root = provider.root();

        System.out.println(root.accept(gen));

        System.out.println(root.goTo(RDF.first).accept(gen));

        System.out.println(root.goTo(RDF.first).fwd().accept(gen));

        System.out.println(root.goTo(RDF.first).fwd(RDFS.label).accept(gen));

        System.out.println(root.goTo(RDF.first).fwd(RDFS.label).dft().accept(gen));

        System.out.println(root.goTo(RDF.first).fwd(RDFS.label).dft().goTo("urn:foo").accept(gen));


        Model m = RDFDataMgr.loadModel("dcat-ap_2.0.0_shacl_shapes.ttl");
        SHNodeShape sourceNode = m.createResource("http://data.europa.eu/r5r#Catalog_Shape")
                .as(SHNodeShape.class);


        if (false) {
            TravProviderTriple<Set<RDFNode>> shaclProvider = createSimpleShaclTraverser(sourceNode);
            dfs(shaclProvider.root(), 0, 9);
        }


        Trav3Provider<Node, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>> myProvider = createSimpleShaclTraverser3(sourceNode);
        dfs(myProvider.newRoot(PathOpsNode.get().newRoot()), 0, 9);

    }

    public static void dfs(Trav<Node, Set<RDFNode>> trav, int depth, int maxDepth) {

        if (depth >= maxDepth) {
            return;
        }

        Iterable<RDFNode> it = trav.state();
        for (RDFNode item : it) {

            // The class or property itself - rather than the shape

            RDFNode target = null;
            /*
            switch (trav.type()) {
            case VALUES:
                target = Optional.ofNullable(item.asResource().getProperty(SH.targetClass))
                .map(Statement::getObject).orElse(null);
                break;
            case PROPERTY:
                target = item.as(SHPropertyShape.class).getPath();
                break;
            default:
                target = item;
                break;
            }
            */


            RDFNode shape = ResourceUtils.asBasicRdfNode(item);
            RDFNode tgt = ResourceUtils.asBasicRdfNode(target);
//            System.out.println("ITEM at depth " + depth + ": " + tgt + " (" + shape + ")");
            System.out.println("ITEM at depth " + depth + ": " + trav.path() + ": " + tgt + " (" + shape + ")");

            Node node = item.asNode();
            Trav<Node, Set<RDFNode>> next = trav.traverse(node);
            dfs(next, depth + 1, maxDepth);
        }
    }



    public static Trav3Provider<Node, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>> createSimpleShaclTraverser3(SHNodeShape rootShape) {

        Trav3Provider<Node, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>> provider = new Trav3Provider<>() {

            @Override
            public Set<RDFNode> mkRoot() {
                return Collections.singleton(rootShape);
            }

            @Override
            public Set<RDFNode> toB(Trav3A<Node, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>> a, Node segment) {
                Set<RDFNode> result = new LinkedHashSet<>();
                result.add(ModelUtils.convertGraphNodeToRDFNode(TravDirection.FWD, rootShape.getModel()));
                result.add(ModelUtils.convertGraphNodeToRDFNode(TravDirection.BWD, rootShape.getModel()));
                return result;
            }

            @Override
            public Set<RDFNode> toC(Trav3B<Node, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>> b, Node segment) {
              Set<RDFNode> nodeShapes = b.parent().state();
              // Node reachingSegment = b.parent().reachingSegment();
              boolean isFwd = segment.equals(TravDirection.FWD);

              Set<RDFNode> propertyShapes = new LinkedHashSet<>();
              for (RDFNode nodeShape : nodeShapes) {
                  SHNodeShape ns = nodeShape.as(SHNodeShape.class);
                  List<RDFNode> list = ns.getPropertyShapes().stream()
                          .filter(ps ->  {
                              // TODO This does not deal with all paths
                              // If dir is backwards (false) then the path has to start with an inversePath
                              return !isFwd
                                      ? Optional.ofNullable(ps.getPath())
                                              .map(p -> p.equals(SHACLM.inversePath))
                                              .orElse(false)
                                      : true;
                          })
                          .collect(Collectors.toList());

                  propertyShapes.addAll(list);
              }

              return propertyShapes;
            }

            @Override
            public Set<RDFNode> toA(Trav3C<Node, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>, Set<RDFNode>> c, Node segment) {

              Node filterProperty = segment;

              //boolean isFwd = from.reachedByFwd();
              // Set<RDFNode> propertyShapes = from.parent().state();

              // We don't need the prior set of computed property shapes
              // We just need property shape that matches the reaching node
              Collection<RDFNode> propertyShapes = Collections.singleton(
                      rootShape.getModel().wrapAsResource(filterProperty)
              );


              // Map each property shape to its  sh:class's shapes
              Set<RDFNode> targetNodeShapes = propertyShapes.stream()
                      .map(p -> p.as(SHPropertyShape.class))
//                      .filter(ps -> {
//                          boolean r = ps.asNode().equals(filterProperty);
//                          return r;
//                          boolean r = Optional.ofNullable(ps.getPath()).map(pa -> {
//                              return pa.asNode().equals(filterProperty);
//                          }).orElse(false);
//                          return r;
//                      })
                      .map(ps -> ps.getPropertyResourceValue(SH.class_))
                      .filter(shClassValue -> shClassValue != null)
                      .flatMap(shClassValue -> {
                          Set<RDFNode> nodeShapes = ResourceUtils.listReverseProperties(shClassValue, SH.targetClass)
                                  .mapWith(Statement::getSubject)
                                  .mapWith(x -> (RDFNode)x)
                                  .toSet();
                          return nodeShapes.stream();
                      })
                      // .map(RDFNode::asResource)
                      .collect(Collectors.toSet());

          return targetNodeShapes;
       }


        };


        return provider;
    }



    public static TravProviderTriple<Set<RDFNode>> createSimpleShaclTraverser(SHNodeShape rootShape) {

        TravTripleStateComputer<Set<RDFNode>> shaclState = new TravTripleStateComputer<>() {
            @Override
            public Set<RDFNode> nextState(TravValues<Set<RDFNode>> from, Node value) {
                Set<RDFNode> result = new LinkedHashSet<>();
                result.add(ModelUtils.convertGraphNodeToRDFNode(TravDirection.FWD, rootShape.getModel()));
                result.add(ModelUtils.convertGraphNodeToRDFNode(TravDirection.BWD, rootShape.getModel()));
                return result;
            }

            // Compute all property shapes
            @Override
            public Set<RDFNode> nextState(TravDirection<Set<RDFNode>> from, boolean isFwd) {
                Set<RDFNode> nodeShapes = from.parent().state();

                Set<RDFNode> propertyShapes = new LinkedHashSet<>();
                for (RDFNode nodeShape : nodeShapes) {
                    SHNodeShape ns = nodeShape.as(SHNodeShape.class);
                    List<RDFNode> list = ns.getPropertyShapes().stream()
                            .filter(ps ->  {
                                // TODO This does not deal with all paths
                                // If dir is backwards (false) then the path has to start with an inversePath
                                return !isFwd
                                        ? Optional.ofNullable(ps.getPath())
                                                .map(p -> p.equals(SHACLM.inversePath))
                                                .orElse(false)
                                        : true;
                            })
                            .collect(Collectors.toList());

                    propertyShapes.addAll(list);
                }

                return propertyShapes;

            }


            // Compute all target node shapes
            @Override
            public Set<RDFNode> nextState(TravProperty<Set<RDFNode>> from, Node property) {
                return Collections.singleton(ModelUtils.convertGraphNodeToRDFNode(TravAlias.DEFAULT_ALIAS, rootShape.getModel()));
            }

            @Override
            public Set<RDFNode> nextState(TravAlias<Set<RDFNode>> from, Node alias) {
                Set<RDFNode> targetNodeShapes;
                if (TravAlias.DEFAULT_ALIAS.equals(alias)) {

                    Node filterProperty = from.reachingPredicate();

                    //boolean isFwd = from.reachedByFwd();
                    // Set<RDFNode> propertyShapes = from.parent().state();

                    // We don't need the prior set of computed property shapes
                    // We just need property shape that matches the reaching node
                    Collection<RDFNode> propertyShapes = Collections.singleton(
                            rootShape.getModel().wrapAsResource(filterProperty)
                    );


                    // Map each property shape to its  sh:class's shapes
                    targetNodeShapes = propertyShapes.stream()
                            .map(p -> p.as(SHPropertyShape.class))
//                            .filter(ps -> {
//                                boolean r = ps.asNode().equals(filterProperty);
//                                return r;
//                                boolean r = Optional.ofNullable(ps.getPath()).map(pa -> {
//                                    return pa.asNode().equals(filterProperty);
//                                }).orElse(false);
//                                return r;
//                            })
                            .map(ps -> ps.getPropertyResourceValue(SH.class_))
                            .filter(shClassValue -> shClassValue != null)
                            .flatMap(shClassValue -> {
                                Set<RDFNode> nodeShapes = ResourceUtils.listReverseProperties(shClassValue, SH.targetClass)
                                        .mapWith(Statement::getSubject)
                                        .mapWith(x -> (RDFNode)x)
                                        .toSet();
                                return nodeShapes.stream();
                            })
                            // .map(RDFNode::asResource)
                            .collect(Collectors.toSet());
                } else {
                    targetNodeShapes = Collections.emptySet();
                }

                return targetNodeShapes;
            }
        };

        TravProviderTriple<Set<RDFNode>> provider =
                TravProviderTripleImpl.create(Collections.singleton(rootShape), shaclState);

        return provider;
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
