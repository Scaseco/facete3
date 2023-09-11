package org.aksw.facete3.app.vaadin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.Directed;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.FacetedQueryResource;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.impl.HLFacetConstraintImpl;
import org.aksw.facete.v3.plugin.JenaPluginFacete3;
import org.aksw.facete3.table.mapping.domain.ColumnMapping;
import org.aksw.facete3.table.mapping.domain.TableMapping;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.entity.graph.metamodel.MainPlaygroundResourceMetamodel;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.model.entityinfo.plugin.JenaPluginEntityInfo;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.datatype.RDFDatatypePPath;
import org.aksw.jenax.path.datatype.RDFDatatypePathNode;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class Facete3Wrapper {

    private FacetDirNode facetDirNode;
    private FacetedQuery facetedQuery;

    /** Base concept = Initial concept + search restriction */
    private UnaryRelation initialConcept;

    protected Set<FacetDirNode> alwaysVisibleCustomFacets = new LinkedHashSet<>();
    protected Multimap<FacetPath, FacetDirNode> customFacetsVisibleAtPath = LinkedHashMultimap.create();

    // For which focus path to show facets of which FacetDirNode
    protected Map<FacetPath, FacetDirNode> focusToFacetDir = new HashMap<>();

    // FIXME The selectedFacet is specific to the facet value list component and thus
    // belongs to the view model of that component

    // FIXME This needs to be a FacetNode (a FacetPath would point wherever if there were changes to the root)
    private Node selectedFacet;

    public UnaryRelation getInitialConcept() {
        return initialConcept;
    }

    public void setInitialConcept(UnaryRelation initialConcept) {
        this.initialConcept = initialConcept;
    }

    public FacetDirNode getFacetDirNode() {
        return facetDirNode;
    }

    public void setFacetDirNode() {
        this.facetDirNode = facetedQuery.focus()
                .fwd();
    }

    public void setFacetDirNode(FacetDirNode facetDirNode) {
        this.facetDirNode = facetDirNode;
    }

    public FacetedQuery getFacetedQuery() {
        return facetedQuery;
    }

    public Node getSelectedFacet() {
        return selectedFacet;
    }

    public void setSelectedFacet(Node facet) {
        selectedFacet = facet;
    }

    public void setBaseConcept(UnaryRelation baseConcept) {
        facetedQuery = facetedQuery.baseConcept(baseConcept);
    }

    public Map<FacetPath, FacetDirNode> getFocusToFacetDir() {
        return focusToFacetDir;
    }

    public Facete3Wrapper(RdfDataSource dataSource) {
    // public Facete3Wrapper(RDFConnection connection) {
        initJena();
        initFacetedQuery(dataSource);
        setEmptyBaseConcept();
        setFacetDirNode();
        setSelectedFacet(RDF.type.asNode());
    }

    public static void initJena() {
        JenaSystem.init();
        JenaPluginFacete3.init();

        TypeMapper.getInstance().registerDatatype(new RDFDatatypePPath());
        TypeMapper.getInstance().registerDatatype(new RDFDatatypePathNode());

        // JenaPluginConjure.init();

        MainPlaygroundResourceMetamodel.init();

        // FIXME Move to separate domain view plugin init method
        BuiltinPersonalities.model.add(RdfDataRefSparqlEndpoint.class,
                JenaPluginUtils.createImplementation(RdfDataRefSparqlEndpoint.class, DefaultPrefixes.get()));

        BuiltinPersonalities.model.add(ServiceStatus.class,
                JenaPluginUtils.createImplementation(ServiceStatus.class, PrefixMapping.Standard));


        // FIXME Move to separate domain view plugin init method
        BuiltinPersonalities.model.add(TableMapping.class,
                JenaPluginUtils.createImplementation(TableMapping.class, PrefixMapping.Extended));

        BuiltinPersonalities.model.add(ColumnMapping.class,
                JenaPluginUtils.createImplementation(ColumnMapping.class, PrefixMapping.Extended));


        Model tmModel = ModelFactory.createDefaultModel();
        TableMapping tm = tmModel.createResource().as(TableMapping.class);
        tm.getOrCreateColumnMapping(Arrays.asList("urn:foo", "urn:bar"));

        RDFDataMgr.write(System.out, tmModel, RDFFormat.TURTLE_PRETTY);
        System.out.println("TODO Remove prior test and its output");




        JenaPluginEntityInfo.init(BuiltinPersonalities.model);
    }

    private void initFacetedQuery(RdfDataSource rdfDataSource) {
        Model dataModel = ModelFactory.createDefaultModel();
        XFacetedQuery xFacetedQuery = dataModel.createResource()
                .as(XFacetedQuery.class);
        FacetedQueryImpl.initResource(xFacetedQuery);
        facetedQuery = FacetedQueryImpl.create(xFacetedQuery, rdfDataSource);
    }

    private void setEmptyBaseConcept() {
        Concept emptyConcept = ConceptUtils.createConcept();
        facetedQuery = facetedQuery.baseConcept(emptyConcept);
    }

    public void activateConstraint(FacetValueCount facetValueCount) {
        getHLFacetConstraint(facetValueCount).setActive(true);
    }

    public void deactivateConstraint(FacetValueCount facetValueCount) {
        getHLFacetConstraint(facetValueCount).setActive(false);
    }

    public HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> getHLFacetConstraint(
            FacetValueCount facetValueCount) {
        return getFacetDirNode().via(facetValueCount.getPredicate())
                .one()
                .enterConstraints()
                .eq(facetValueCount.getValue());
    }

    public void setFacetDirection(org.aksw.facete.v3.api.Direction direction) {
        facetDirNode = facetDirNode.parent()
                .step(direction);
    }

    public void resetPath() {
        FacetNode rootNode = facetDirNode.parent()
                .root();
        changeFocus(rootNode);
    }

    public void addFacetToPath(FacetCount facet) {
        org.aksw.facete.v3.api.Direction dir = facetDirNode.dir();
        Node node = facet.getPredicate();
        facetedQuery.focus()
                .step(node, dir)
                .one()
                .chFocus();
        setFacetDirNode(facetedQuery.focus()
                .step(dir));
    }

    public void changeFocus(FacetNode node) {
        org.aksw.facete.v3.api.Direction dir = node.reachingDirection();
        if (dir == null) {
            dir = facetDirNode.dir();
        }
        node.chFocus();
        setFacetDirNode(node.step(dir));
    }

    public List<Node> getPathNodes() {
        List<Directed<FacetNode>> path = facetDirNode.parent()
                .path();
        List<Node> pathNodes = path.stream()
                .map(Directed::getValue)
                .map(FacetNode::reachingPredicate)
                .collect(Collectors.toList());
        return pathNodes;
    }

    public List<Directed<FacetNode>> getPath() {
        return facetDirNode.parent()
                .path();
    }

    public List<HLFacetConstraint<?>> getFacetConstraints() {
        List<HLFacetConstraint<?>> constraints = new ArrayList<HLFacetConstraint<?>>();
        for (FacetConstraint c : facetedQuery.constraints()) {
            HLFacetConstraint<?> hlc = toHlConstraint(facetedQuery, c);
            // TODO We should add pairs with the facet constraints together with the
            // precomputed string
            // then we can batch the label lookups here
            constraints.add(hlc);
        }
        return constraints;
    }

    private HLFacetConstraint<?> toHlConstraint(FacetedQuery facetedQuery,
            FacetConstraint facetConstraint) {
        FacetedQueryResource r = facetedQuery.as(FacetedQueryResource.class);

        BgpNode HACK = ModelFactory.createDefaultModel()
                .createResource("should not appear anywhere")
                .as(BgpNode.class);


        // HACK FacetNodeImpl requires a bgpNode - but we don't need its value
        // We only need it in order to set up HLFacetConstraint.pathsMentioned
        FacetNode tmp = new FacetNodeImpl(r, HACK);
        HLFacetConstraint<?> result = new HLFacetConstraintImpl<Void>(null, tmp, facetConstraint);
        return result;
    }

//    private BgpNode HACK = ModelFactory.createDefaultModel()
//            .createResource("should not appear anywhere")
//            .as(BgpNode.class);
//

    // TODO Should not be here
    public RDFNode fetchIfResource(Node node) {
        Query query = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");
        UnaryRelation filter = ConceptUtils.createFilterConcept(node);
        query.setQueryPattern(RelationImpl.create(query.getQueryPattern(), Vars.s)
                .joinOn(Vars.s)
                .with(filter)
                .getElement());
        Model model = facetedQuery.connection()
                .queryConstruct(query);
        RDFNode result = model.asRDFNode(node);
        return result;
    }
}

