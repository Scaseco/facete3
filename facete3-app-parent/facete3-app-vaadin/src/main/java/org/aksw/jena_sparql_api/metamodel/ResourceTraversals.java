package org.aksw.jena_sparql_api.metamodel;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.trav.api.Trav;
import org.aksw.commons.path.trav.l2.Trav2Trees.TreeNode2;
import org.aksw.commons.path.trav.l2.Trav2Trees.TreeNode2A;
import org.aksw.commons.path.trav.l2.Trav2Trees.TreeNode2B;
import org.aksw.commons.path.trav.l2.Trav2Trees.TreeNode2Provider;
import org.aksw.commons.path.trav.l2.Trav2Trees.TreeNode2ProviderBase;
import org.aksw.commons.path.trav.l2.Trav2Trees.TreeNode2Visitor;
import org.aksw.commons.path.trav.l3.Trav3.Trav3A;
import org.aksw.commons.path.trav.l3.Trav3.Trav3B;
import org.aksw.commons.path.trav.l3.Trav3.Trav3C;
import org.aksw.commons.path.trav.l3.Trav3Provider;
import org.aksw.commons.path.trav.l5.Traversals5.Traversal5A;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ClassRelationModel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.DatasetMetamodel;
import org.aksw.jena_sparql_api.entity.graph.metamodel.GraphPredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.PredicateStats;
import org.aksw.jena_sparql_api.entity.graph.metamodel.ResourceGraphMetamodel;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.schema.NodeSchemaFromNodeShape;
import org.aksw.jena_sparql_api.schema.PropertySchemaFromPropertyShape;
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
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.path.core.PathNode;
import org.aksw.jenax.path.core.PathOpsNode;
import org.aksw.jenax.path.core.PathPE;
import org.aksw.jenax.path.relgen.RelationGeneratorSimple;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.vocabulary.SHACLM;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.vocabulary.SH;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

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
//        PathPE expected = PathOpsPE.newAbsolutePath()
//            .resolveSegment("<urn:test>")
//            .resolveSegment("?x IN (<urn:yay>)");
//
//        String str = expected.toString();
//        System.out.println(expected);
//
//        PathPE actual = PathOpsPE.newAbsolutePath().resolve(str);
//
//        System.out.println(expected.equals(actual));
//
//
//
//        mainShacl(args);

        mainRelationGen(args);
    }

    public static void mainRelationGen(String[] args) {

        Dataset m = RDFDataMgr.loadDataset("dcat-ap_2.0.0_shacl_shapes.ttl");
        Node rootShape = NodeFactory.createURI("http://data.europa.eu/r5r#Catalog_Shape");
        UnaryRelation rootRel = Concept.createNodes(rootShape);

        RDFConnection conn = RDFConnectionFactory.connect(m);

        Relation shaclRelation = createShaclRelation();
        Table<Boolean, Node, Set<Resource>> pToDirToTgtShape = getShaclTransitions(rootRel, conn);


        System.out.println(pToDirToTgtShape);



        RelationGeneratorSimple gen = RelationGeneratorSimple.create(shaclRelation);

        // PathPE exprs = PathOpsPE.newAbsolutePath().resolveAll();






        PathNode path = PathNode.newAbsolutePath();
        PathNode tgt = path.resolve(RDF.first).resolve(RDF.rest).resolve(RDFS.label).resolve(RDF.type).resolve(OWL.hasValue);

        XRelationTree tree = new XRelationTree();
        TreeNode2<Node, XRelationNode, XAliasNode> node = tree.resolve(tgt);

        String alias = tree.root().child(RDF.Nodes.first).child(RDF.Nodes.rest).child(RDFS.Nodes.label).getAlias();
        System.out.println("ALIAS IS " + alias);


        TreeNode2Visitor<String, XRelationNode, XAliasNode> visitor = new TreeNode2Visitor<String, XRelationNode, XAliasNode>() {
            @Override
            public String visitA(XRelationNode a) {
                return "" + a.getRelation();
            }

            @Override
            public String visitB(XAliasNode b) {
                return b.getAlias();
            }
        };

        System.out.println("CHILDREN: " + tree.root().childKeys().collect(Collectors.toList()));

        System.out.println("GOT: " + node.accept(visitor));

        if (node.isA()) {
            XRelationNode relNode = node.asA();
            System.out.println(relNode.getRelation());
        } else if (node.isB()) {
            XAliasNode alNode = node.asB();
            System.out.println(alNode.getAlias());
        }



        Relation r = createShaclRelation();
        System.out.println(r);




        PathPE exprs = PathPE.newAbsolutePath()
                .resolve(rootShape)
                .resolveSegment("<urn:fwd>")
                .resolve(DCAT.dataset)
                .resolveAll()
                .resolveSegment("<urn:fwd>")
                .resolve(DCAT.distribution)
                .resolveAll()
                .resolveSegment("<urn:fwd>")
                ;   ;//.resolve(RDFS.label).resolveAll().resolveSegment("<urn:fwd>");


        System.out.println(exprs);
        gen.process(exprs);

        Query q = gen.getCurrentConcept().toQuery();
        q.setDistinct(true);

//        System.out.println(q);

        //System.out.println(rel);

         Query qp = QueryUtils.applyOpTransform(q, AlgebraUtils.createDefaultRewriter()::rewrite);
         System.out.println(qp);

         System.out.println("Result set:");
         conn.querySelect(qp, row -> System.out.println(row));
    }


    /**
     * From a given start concept matching a set of source
     * shacl NodeShapes, return a table target node shapes via
     * predicate and direction.
     *
     * @param rootRel
     * @param conn
     * @return
     */
    public static Table<Boolean, Node, Set<Resource>> getShaclTransitions(UnaryRelation rootRel, RDFConnection conn) {

        Relation shaclRelation = createShaclRelation();

        List<Var> vars = shaclRelation.getVars();
        Relation filtered = shaclRelation.prependOn(vars.get(0)).with(rootRel);

        System.out.println("Filtered: " + filtered);


        Table<Boolean, Node, Set<Resource>> pToDirToTgtShape = HashBasedTable.create();
        conn.querySelect(filtered.toQuery(), row -> {
            Node p = row.get("p").asNode();
            Resource tgt = row.getResource("tgt");
            boolean isFwd = row.getResource("dir").getURI().equals("urn:fwd");
            System.out.println("" + p + "  " + isFwd + " " + tgt);
            Set<Resource> tgtShapes = pToDirToTgtShape.row(isFwd).computeIfAbsent(p, k -> new HashSet());

            if (tgt != null) {
                tgtShapes.add(tgt);
            }
        });
        return pToDirToTgtShape;
    }


    public static Relation createShaclRelation() {

        Query query = SparqlStmtMgr.loadQuery("shacl-relation.rq");
        Relation result = RelationUtils.fromQuery(query);

        return result;
    }



    static class XRelationTree
        extends TreeNode2ProviderBase<Node, XRelationNode, XAliasNode>
    {

        @Override
        public XRelationNode mkRoot() {
            return new XRelationNode(PathNode.newAbsolutePath(), this, null, RelationUtils.SPO);
        }

        @Override
        public XAliasNode toB(XRelationNode a, Node segment) {
            String alias = segment.toString();
            return new XAliasNode(a.path().resolve(segment), this, a, alias);
        }

        @Override
        public XRelationNode toA(XAliasNode b, Node segment) {
            return new XRelationNode(b.path().resolve(segment), this, b, RelationUtils.SPO);
        }
    }

    static class XAliasNode
        extends TreeNode2B<Node, XRelationNode, XAliasNode>
    {
        protected String alias;

        public XAliasNode(Path<Node> path, TreeNode2Provider<Node, XRelationNode, XAliasNode> provider,
                XRelationNode parent, String alias) {
            super(path, provider, parent);
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

        @Override
        protected XRelationNode sendSelfToProvider(Node key) {
            return provider.toA(this, key);
        }
    }

    static class XRelationNode
        extends TreeNode2A<Node, XRelationNode, XAliasNode>
    {
        protected Relation relation;

        public XRelationNode(Path<Node> path, TreeNode2Provider<Node, XRelationNode, XAliasNode> provider,
                XAliasNode parent, Relation relation) {
            super(path, provider, parent);
            this.relation = relation;
        }

        public Relation getRelation() {
            return relation;
        }

        @Override
        protected XAliasNode sendSelfToProvider(Node key) {
            return provider.toB(this, key);
        }
    }






    public static void mainShacl(String[] args) {
        JenaSystem.init();
        SHFactory.ensureInited();

        JenaPluginUtils.registerResourceClasses(
                NodeSchemaFromNodeShape.class,
                PropertySchemaFromPropertyShape.class,
                DatasetMetamodel.class,
                ResourceGraphMetamodel.class,
                PredicateStats.class,
                GraphPredicateStats.class,

                ClassRelationModel.class,
                ResourceGraphMetamodel.class
                );


        System.out.println( Paths.get("/tmp").relativize(Paths.get("/tmp/foo")) );
        System.out.println( Paths.get("/tmp/foo").relativize(Paths.get("/tmp")) );

        PathNode r = PathNode.newAbsolutePath();

        PathNode path = r.resolve(RDF.type).resolve(RDF.first).resolve(NodeValue.makeInteger(1).asNode());

        System.out.println(path);

        Path<Node> rel = r.relativize(path);
        String relStr = rel.toString();
        System.out.println(relStr);
        path = r.resolveStr(relStr);
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
        dfs(myProvider.newRoot(PathNode.newAbsolutePath()), 0, 9);

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



    /***
     * Model: /${direction}/${property}$
     *
     * Note: Immediate backward traversals should be filtered out?!
     *
     * Property shape is usually the bnode id!
     *
     *
     * @param rootShape
     * @return
     */




    /***
     * Model: /${nodeShape}/${direction}/${propertyShape}/${nodeSape}
     *
     * Property shape is usually the bnode id!
     *
     *
     * @param rootShape
     * @return
     */
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
