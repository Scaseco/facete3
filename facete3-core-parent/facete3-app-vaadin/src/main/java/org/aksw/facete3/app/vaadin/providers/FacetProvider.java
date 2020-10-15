package org.aksw.facete3.app.vaadin.providers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.aksw.facete3.app.vaadin.LabelService;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public abstract class FacetProvider<T extends RDFNode> extends AbstractBackEndDataProvider<T, Void>
        implements ConfigurableFilterDataProvider<T, Void, String> {

    private static final long serialVersionUID = 1L;
    private LabelService labelService;
    protected final Facete3Wrapper facete3;
    private Function<? super T, ? extends Node> nodeForLabelFunction;
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

    public Facete3Wrapper getFacete3() {
        return facete3;
    }

    public FacetProvider(Facete3Wrapper facete3, LabelService labelService) {
        this.facete3 = facete3;
        this.labelService = labelService;
        nodeForLabelFunction = getNodeForLabelFunction();
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
        Integer limit = query.getLimit() == Integer.MAX_VALUE ? null : query.getLimit();

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
            }

            dataQuery.addOrderBy(new NodePathletPath(path), sortDir);
        }
        List<T> list = dataQuery.limit(limit)
                .offset(query.getOffset())
                .exec()
                .toList()
                .doOnSuccess(item -> labelService.enrichWithLabels(item, nodeForLabelFunction))
                .blockingGet();
        Stream<T> stream = list.stream();
        return stream;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    public static String getLabel(RDFNode node) {
        return LabelService.getLabel(node);
    }

    public static <T> Map<T, String> getLabels(Collection<T> rdfNodes,
            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
        return LabelService.getLabels(rdfNodes, defineNodeForLabelFunction);
    }
}
