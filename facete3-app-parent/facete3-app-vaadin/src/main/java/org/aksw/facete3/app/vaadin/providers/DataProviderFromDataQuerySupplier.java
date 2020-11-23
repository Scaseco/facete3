package org.aksw.facete3.app.vaadin.providers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public abstract class DataProviderFromDataQuerySupplier<T extends RDFNode>
    extends AbstractBackEndDataProvider<T, String> {
    //implements ConfigurableFilterDataProvider<T, Void, String> {

    private static final Logger logger = LoggerFactory.getLogger(DataProviderFromDataQuerySupplier.class);

    private static final long serialVersionUID = 1L;
//    protected LabelService labelService;
//    private String filter;
//
//    @Override
//    public void setFilter(String filter) {
//        this.filter = filter;
//        refreshAll();
//    }
//
//
//    public String getFilter() {
//        if (filter == null) {
//            filter = "";
//        }
//        return filter;
//    }


//    public DataProviderFromDataQuerySupplier(LabelService labelService) {
//        this.labelService = labelService;
//    }

    protected abstract DataQuery<T> getDataQuery();
    protected abstract void applyFilter(DataQuery<T> dataQuery , String filterStr);

    protected DataQuery<T> translateQuery(Query<T, String> query) {

        DataQuery<T> dataQuery = getDataQuery();
        String filter = query.getFilter().orElse("");

        if (!filter.isEmpty()) {
            applyFilter(dataQuery, filter);
        }

        return dataQuery;
    }
//    protected abstract Node getLabelNode(RDFNode rdfNode);

    @Override
    protected int sizeInBackEnd(Query<T, String> query) {
        DataQuery<T> dataQuery = translateQuery(query);
        long count = dataQuery.count()
                .timeout(60, TimeUnit.SECONDS)
                .blockingGet()
                .getCount();
        int countAsInt = (int) Math.min(count, Integer.MAX_VALUE);
        return countAsInt;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, String> query) {
        DataQuery<T> dataQuery = translateQuery(query);
        List<QuerySortOrder> sortOrders = query.getSortOrders();

        int o = query.getOffset();
        int l = query.getLimit();

        Integer offset = o == 0 ? null : o;
        Integer limit = l == Integer.MAX_VALUE ? null : l;


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
        List<T> list = dataQuery
                .limit(limit)
                .offset(offset)
                .exec()
                .toList()
//                .doOnSuccess(item -> labelService.enrichWithLabels(item, rdfNodeToLabelNode))
                .blockingGet();

        if (list.size() > limit) {
            logger.warn("Assertion failed: Requested limit: " + limit + " offset: " + query.getOffset() + "; got items: " + list.size());
            logger.warn("Query was: " + dataQuery.toConstructQuery());

            list = list.stream()
                .limit(limit)
                .collect(Collectors.toList());
        }

        Stream<T> stream = list.stream();
        return stream;
    }

    @Override
    public boolean isInMemory() {
        return false;
    }
}

