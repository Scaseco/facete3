package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.facete3.app.shared.viewselector.EntityClassifier;
import org.aksw.facete3.app.shared.viewselector.ViewTemplate;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.rx.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.ElementData;

import com.vaadin.flow.component.Component;

/**
 *
 * TODO Refactor into a generic lookup class for T findBestMatch(Concept)
 *
 * @author raven
 *
 */
public class ViewManagerImpl
    implements ViewManager
{
    protected Map<Node, ViewFactory> viewFactories = new LinkedHashMap<>();

    protected SparqlQueryConnection conn;

    public ViewManagerImpl(SparqlQueryConnection conn) {
        super();
        this.conn = conn;
    }


    @Override
    public void register(ViewFactory viewFactory) {
        viewFactories.put(viewFactory.getViewTemplate().getMetadata().asNode(), viewFactory);
    }


    protected EntityClassifier buildClassifier() {
        EntityClassifier result = new EntityClassifier(Vars.s);

        for (Entry<Node, ViewFactory> e : viewFactories.entrySet()) {

            Node node = e.getKey();
            ViewFactory viewFactory = e.getValue();

            ViewTemplate viewTemplate = viewFactory.getViewTemplate();

//            Resource metadata = viewTemplate.getMetadata();
            //Node node = metadata.asNode();

            UnaryRelation condition = viewTemplate.getCondition();

            result.addCondition(node, condition);
        }

        return result;
    }

    public static Query createStandardQuery(Var entityVar, Node node) {
        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(new ElementData(
                Collections.singletonList(entityVar),
                Collections.singletonList(BindingFactory.binding(Vars.s, node))));

        return query;
    }

    public static EntityQueryImpl createEntityQuery(Var entityVar, Query standardQuery) {
        EntityBaseQuery ebq = new EntityBaseQuery(Collections.singletonList(entityVar), new EntityTemplateImpl(), standardQuery);
        EntityQueryImpl result = new EntityQueryImpl();
        result.setBaseQuery(ebq);

        return result;
    }

    public static EntityQueryImpl createEntityQuery(EntityGraphFragment entityGraphFragment, Node node) {
        Var entityVar = Vars.s;

        Query standardQuery = createStandardQuery(entityVar, node);
        EntityQueryImpl result = createEntityQuery(entityVar, standardQuery);

        result.getAuxiliaryGraphPartitions().add(new GraphPartitionJoin(entityGraphFragment));

        return result;
    }

    @Override
    public ViewFactory getBestViewFactory(Node node) {
        Set<Node> classifications = getClassifications(node);
        Node match = Iterables.getFirst(classifications, null);
        ViewFactory result = match == null ? null : viewFactories.get(match);

        return result;
    }


    public Set<Node> getClassifications(Node node) {
        EntityClassifier entityClassifier = buildClassifier();
        EntityGraphFragment entityGraphFragment = entityClassifier.createGraphFragment();

        EntityQueryImpl entityQuery = createEntityQuery(entityGraphFragment, node);

        // Fetch the classifications for the given node
        List<RDFNode> results = EntityQueryRx.execConstructEntities(conn, entityQuery).toList().blockingGet();

        // Flat collection; TODO collect into a Map<Node, Collection<Node>>
        Set<Node> classifications = results.stream()
            .filter(RDFNode::isResource)
            .map(RDFNode::asResource)
            .flatMap(r -> r.listProperties(EntityClassifier.classifier).toList().stream()
                    .map(Statement::getObject)
                    .map(RDFNode::asNode))
            .collect(Collectors.toSet());


        return classifications;
    }

    @Override
    public Component getComponent(Node node) {
        Component result = null;
        ViewFactory viewFactory = getBestViewFactory(node);

        if (viewFactory != null) {
            Resource data = fetchData(node, viewFactory);

            if (data != null) {
                result = viewFactory.createComponent(data);
            }
        }

        return result;
    }

    public SparqlQueryConnection getConnection() {
        return conn;
    }


    public Resource fetchData(Node node, ViewFactory viewFactory) {
        EntityQueryImpl viewEntityQuery = viewFactory.getViewTemplate().getEntityQuery();

        RDFNode tmp = fetchData(conn, node, viewEntityQuery);

        // The RDFNode must be a resource otherwise an exception is raised
        Resource result = tmp == null ? null : tmp.asResource();
        return result;
    }

    public static RDFNode fetchData(SparqlQueryConnection conn, Node node, EntityQueryImpl viewEntityQuery) {


        Var entityVar = Vars.s;
        Query standardQuery = createStandardQuery(entityVar, node);
        EntityQueryImpl entityQuery = createEntityQuery(Vars.s, standardQuery);

        entityQuery.getAuxiliaryGraphPartitions().addAll(viewEntityQuery.getAuxiliaryGraphPartitions());
        entityQuery.getOptionalJoins().addAll(viewEntityQuery.getOptionalJoins());


        List<RDFNode> entities = EntityQueryRx.execConstructEntities(conn, entityQuery)
            .toList().blockingGet();


        // One result expected
        RDFNode data = Cardinalities.expectZeroOrOne(entities.stream()).orElse(null);
        return data;
    }

    @Override
    public List<ViewFactory> getApplicableViewFactories(Node node) {
        Set<Node> classifications = getClassifications(node);

        List<ViewFactory> result = classifications.stream()
            .map(viewFactories::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return result;
    }

//    public ViewFactory prepare(UnaryRelation concept) {
//
//    }
}
