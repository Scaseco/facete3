package org.aksw.facete3.app.vaadin.providers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDFS;

public abstract class FacetProvider<T extends RDFNode> extends AbstractBackEndDataProvider<T, Void>
        implements ConfigurableFilterDataProvider<T, Void, String> {

    private static final long serialVersionUID = 1L;
    private static PrefixMapping prefixes;
    protected final QueryConf queryConf;
    private Function<? super T, ? extends Node> nodeForLabelFunction;
    private static LookupService<Node, String> labelLookupService;
    private String filter;

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
        refreshAll();
    }


    public String getFilter() {
        if (filter == null) {
            filter = "";
        }
        return filter;
    }


    public FacetProvider(QueryConf queryConf) {
        this.queryConf = queryConf;
        prefixes = new PrefixMappingImpl();
        labelLookupService = getLabelLookupService();
        nodeForLabelFunction = getNodeForLabelFunction();
    }

    private LookupService<Node, String> getLabelLookupService() {
        RDFConnection connection = queryConf.getConnection();
        BinaryRelation labelRelation = BinaryRelationImpl.create(RDFS.label);
        return LookupServiceUtils.createLookupService(connection, labelRelation)
                .partition(10)
                .cache()
                .mapValues(this::getLabelFromLookup);
    }

    private String getLabelFromLookup(Node node, List<Node> results) {
        String label = "";
        if (!results.isEmpty()) {
            Node labelNode = results.get(0);
            label = labelNode.toString();
        } else {
            String uri = node.getURI();
            label = deriveLabelFromIri(uri);
        }
        return label;
    }

    protected abstract DataQuery<T> translateQuery(Query<T, Void> query);

    protected abstract Function<? super T, ? extends Node> getNodeForLabelFunction();

    @Override
    protected int sizeInBackEnd(Query<T, Void> query) {
        DataQuery<T> dataQuery = translateQuery(query);
        long count = dataQuery.count()
                .timeout(60, TimeUnit.SECONDS)
                .blockingGet()
                .getCount();
        int countAsInt = (int) Math.min(count, Integer.MAX_VALUE);
        return countAsInt;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
        DataQuery<T> dataQuery = translateQuery(query);
        List<QuerySortOrder> sortOrders = query.getSortOrders();
        int limit = query.getLimit() - 1;
        if (!sortOrders.isEmpty()) {
            QuerySortOrder sortOrder = sortOrders.get(0);
            // 0 = Ascending, 1 = Descending
            SortDirection vaadiDirection = sortOrder.getDirection();
            String column = sortOrder.getSorted();
            int sortDir = vaadiDirection == SortDirection.ASCENDING
                    ? org.apache.jena.query.Query.ORDER_ASCENDING
                    : org.apache.jena.query.Query.ORDER_DESCENDING;
            Path path = Path.newPath();
            if (!column.isEmpty()) {
                path = path.fwd("http://www.example.org/" + column);
            } else {
                limit += limit;
            }
            dataQuery.addOrderBy(new NodePathletPath(path), sortDir);
        }
        List<T> list = dataQuery.limit(limit)
                .offset(query.getOffset())
                .exec()
                .toList()
                .doOnSuccess(item -> enrichWithLabels(item, nodeForLabelFunction))
                .blockingGet();
        Stream<T> stream = list.stream();
        return stream;
    }


    @Override
    public boolean isInMemory() {
        return false;
    }

    public static <T extends RDFNode> void enrichWithLabels(Collection<T> rdfNodes,
            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
        Multimap<Node, T> index = Multimaps.index(rdfNodes, defineNodeForLabelFunction::apply);
        Set<Node> s = index.keySet()
                .stream()
                .filter(Node::isURI)
                .collect(Collectors.toSet());
        Map<Node, String> map = labelLookupService.fetchMap(s);
        index.forEach((k, v) -> v.asResource()
                .addLiteral(RDFS.label,
                        map.getOrDefault(k,
                                NodeUtils.nullUriNode.equals(k) ? "(null)"
                                        : k.isURI() ? deriveLabelFromIri(k.getURI())
                                                : formatLiteralNode(k, prefixes))));
    }

    public static <T extends RDFNode> Map<T, String> getLabels(Collection<T> rdfNodes,
            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
        Multimap<Node, T> index = Multimaps.index(rdfNodes, defineNodeForLabelFunction::apply);
        Set<Node> labelNodes = index.keySet()
                .stream()
                .filter(Node::isURI)
                .collect(Collectors.toSet());
        Map<Node, String> map = labelLookupService.fetchMap(labelNodes);

        Function<Node, String> determineLabel = k -> map.getOrDefault(k,
                k.isURI() ? deriveLabelFromIri(k.getURI()) : formatLiteralNode(k, prefixes));

        Map<T, String> result = index.entries()
                .stream()
                .map(e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

    public static String deriveLabelFromIri(String iriStr) {

        String result;
        for (;;) {
            // Split XML returns invalid out-of-bound index for
            // <http://dbpedia.org/resource/Ada_Apa_dengan_Cinta%3>
            // This is what Node.getLocalName does
            int idx = Util.splitNamespaceXML(iriStr);
            result = idx == -1 || idx > iriStr.length() ? iriStr : iriStr.substring(idx);
            if (result.isEmpty() && !iriStr.isEmpty() && idx != -1) {
                iriStr = iriStr.substring(0, iriStr.length() - 1);
                continue;
            } else {
                break;
            }
        } ;
        return result;
    }

    public static String formatLiteralNode(Node node, PrefixMapping prefixMapping) {
        String result;
        if (node.isLiteral()) {
            String dtIri = node.getLiteralDatatypeURI();
            String dtPart = null;
            if (dtIri != null) {
                Entry<String, String> prefixToIri =
                        PrefixUtils.findLongestPrefix(prefixMapping, dtIri);
                dtPart = prefixToIri != null
                        ? prefixToIri.getKey() + ":" + dtIri.substring(prefixToIri.getValue()
                                .length())
                        : "<" + dtIri + ">";
            }

            result = "\"" + node.getLiteralLexicalForm() + "\""
                    + (dtPart == null ? "" : "^^" + dtPart);
        } else {
            result = Objects.toString(node);
        }

        return result;
    }

    public static String getLabel(RDFNode node) {
        Resource r = node.isResource() ? node.asResource() : null;

        String result = r != null ? Optional.ofNullable(r.getProperty(RDFS.label))
                .map(Statement::getString)
                .orElse(r.isURIResource() ? deriveLabelFromIri(r.getURI())
                        : r.getId()
                                .getLabelString())
                : Objects.toString(Optional.ofNullable(node)
                        .map(RDFNode::asNode)
                        .orElse(null));

        // System.out.println("RESULT: " + result + " for " + node);
        return result;
    }

}
