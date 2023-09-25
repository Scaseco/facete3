package org.aksw.facete3.app.vaadin.plugin.view;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRxBuilder;
import org.aksw.jena_sparql_api.rx.entity.model.AttributeGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.datashape.viewselector.ViewTemplate;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.dataaccess.rx.ListServiceEntityQuery;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Var;

import com.vaadin.flow.component.Component;

/**
 *
 * TODO Refactor into a generic lookup class for T findBestMatch(Concept)
 *
 * @author raven
 *
 */
public class ViewManagerImpl
    implements ViewManagerBulk
{
    protected Map<Node, ViewFactory> viewFactories = new LinkedHashMap<>();

    protected QueryExecutionFactoryQuery qef;

    public ViewManagerImpl(QueryExecutionFactoryQuery qef) {
        super();
        this.qef = qef;
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



    protected Node pickBestClass(Collection<? extends Node> candidates) {
        Node result = Iterables.getFirst(candidates, null);
        return result;
    }

    @Override
    public Map<Node, ViewFactory> getBestViewFactories(Collection<Node> nodes) {
        Map<Node, Set<Node>> classifications = getClassifications(nodes);

        Map<Node, ViewFactory> result = classifications.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                e -> {
                    Node bestClass = pickBestClass(e.getValue());
                    ViewFactory r = bestClass == null ? null : viewFactories.get(bestClass);
                    return r;
                }));

        return result;
    }

    public ListServiceEntityQuery createListService() {
        EntityClassifier entityClassifier = buildClassifier();
        EntityGraphFragment entityGraphFragment = entityClassifier.createGraphFragment();

        AttributeGraphFragment attrPart = new AttributeGraphFragment();
        attrPart.getMandatoryJoins().add(new GraphPartitionJoin(entityGraphFragment));

        ListServiceEntityQuery result = new ListServiceEntityQuery(qef, attrPart);
        return result;
    }


    public Map<Node, Set<Node>> getClassifications(Iterable<Node> nodes) {
        Map<Node, RDFNode> rdfNodeMap = createListService()
                .asLookupService().requestMap(nodes)
                .blockingGet(); //.get(node);

//        EntityClassifier entityClassifier = buildClassifier();
//        EntityGraphFragment entityGraphFragment = entityClassifier.createGraphFragment();
//
//        EntityQueryImpl entityQuery = EntityQueryImpl.createEntityQuery(entityGraphFragment, node);
//
//        // Fetch the classifications for the given node
//        List<RDFNode> results = EntityQueryRx.execConstructEntities(conn, entityQuery).toList().blockingGet();
//
//        // Flat collection; TODO collect into a Map<Node, Collection<Node>>
        Map<Node, Set<Node>> classifications = rdfNodeMap.entrySet().stream()
//            .filter(e -> e != null)
            .filter(e -> e.getValue() != null && e.getValue().isResource())
            .collect(Collectors.toMap(
                    Entry::getKey,
                    e -> e.getValue().asResource().listProperties(EntityClassifier.classifier).toList().stream()
                        .map(Statement::getObject)
                        .map(RDFNode::asNode)
                        .collect(Collectors.toSet()))
                    );

        return classifications;
    }

    @Override
    public Map<Node, Component> getComponents(Collection<Node> nodes) {
        Map<Node, ViewFactory> viewFactories = getBestViewFactories(nodes);

        // Invert the mapping such that each view factory maps to the set
        // related nodes
        Multimap<ViewFactory, Node> index = Multimaps.index(viewFactories.keySet(), viewFactories::get);

        Map<Node, Component> result = index.asMap().entrySet().stream().flatMap(e -> {
            ViewFactory viewFactory = e.getKey();
            Collection<Node> clusteredNodes = e.getValue();

            Map<Node, Resource> nodeToData = fetchData(clusteredNodes, viewFactory);
            Map<Node, Component> nodeToComponent = Maps.transformValues(nodeToData, v -> {
                Component r = v != null
                        ? viewFactory.createComponent(v)
                        : null;

                return r;
            });

            return nodeToComponent.entrySet().stream();

        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));


//        Map<Node, Component> result = Maps.transformEntries(viewFactories, (k, v) -> {
//            Component r = null;
//            if (v != null) {
//                Resource data = fetchData(k, v);
//                if (data != null) {
//                    r = v.createComponent(data);
//                }
//            }
//            return r;
//        });

//        if (viewFactory != null) {
//            Resource data = fetchData(node, viewFactory);
//
//            if (data != null) {
//                result = viewFactory.createComponent(data);
//            }
//        }

        return result;
    }

    public QueryExecutionFactoryQuery getConnection() {
        return qef;
    }


    public Map<Node, Resource> fetchData(Collection<Node> nodes, ViewFactory viewFactory) {
        EntityQueryImpl viewEntityQuery = viewFactory.getViewTemplate().getEntityQuery();

        Map<Node, RDFNode> tmp = fetchData(qef, nodes, viewEntityQuery);

        // The RDFNode must be a resource otherwise an exception is raised
        // Resource result = tmp == null ? null : tmp.asResource();
        Map<Node, Resource> result = Maps.transformValues(tmp, v -> v == null ? null : v.asResource());
        return result;
    }



    public static Map<Node, RDFNode> fetchData(QueryExecutionFactoryQuery qef, Collection<Node> nodes, EntityQueryImpl viewEntityQuery) {


        Var entityVar = Vars.s;
        Query standardQuery = EntityQueryImpl.createStandardQuery(entityVar, nodes);
        EntityQueryImpl entityQuery = EntityQueryImpl.createEntityQuery(Vars.s, standardQuery);

        entityQuery.getMandatoryJoins().addAll(viewEntityQuery.getMandatoryJoins());
        entityQuery.getOptionalJoins().addAll(viewEntityQuery.getOptionalJoins());


        List<RDFNode> entities = EntityQueryRxBuilder.create()
                    .setQueryExecutionFactory(qef).setQuery(entityQuery)
                    .build()
            .toList().blockingGet();

        Map<Node, RDFNode> result = entities.stream()
                .collect(Collectors.toMap(rdfNode -> rdfNode.asNode(), rdfNode -> rdfNode));

        // One result expected
//        RDFNode data = Cardinalities.expectZeroOrOne(entities.stream()).orElse(null);
        return result;
    }

    @Override
    public Map<Node, List<ViewFactory>> getApplicableViewFactories(Collection<Node> nodes) {
        Map<Node, Set<Node>> classifications = getClassifications(nodes);

        Map<Node, List<ViewFactory>> result = Maps.transformValues(classifications, v -> v.stream()
                .map(viewFactories::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

//        Map<Node, List<ViewFactory>> result = classifications.entrySet().stream()
//            .map(viewFactories::get)
//            .filter(Objects::nonNull)
//            .collect(Collectors.toList());

        return result;
    }


//    @Override
//    public Map<Node, Component> getComponents(Iterable<Node> nodes) {
//    }
//    public ViewFactory prepare(UnaryRelation concept) {
//
//    }
}
