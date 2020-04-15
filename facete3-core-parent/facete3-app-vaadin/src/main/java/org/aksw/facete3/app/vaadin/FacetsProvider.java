package org.aksw.facete3.app.vaadin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.apache.jena.rdf.model.RDFNode;

public abstract class FacetsProvider<T extends RDFNode> extends AbstractBackEndDataProvider<T, String> {

    protected final QueryConf queryConf;

    private static final long serialVersionUID = 18L;

    public FacetsProvider(QueryConf queryConf) {
        this.queryConf = queryConf;
    }

    protected abstract DataQuery<T> translateQuery(Query<T, String> query);

    protected int sizeInBackEnd(Query<T, String> query) {
        DataQuery<T> dataQuery = translateQuery(query);
        long count = dataQuery.count().timeout(60, TimeUnit.SECONDS).blockingGet().getCount();
        int countAsInt = (int) Math.min(count, Integer.MAX_VALUE);
        System.out.println(countAsInt);
        return countAsInt;
    }

    protected Stream<T> fetchFromBackEnd(Query<T, String> query) {
        DataQuery<T> dataQuery = translateQuery(query);
        List<QuerySortOrder> sortOrders = query.getSortOrders();
        int limit = query.getLimit() - 1;
        if (sortOrders.isEmpty()) {
            dataQuery.randomOrder();
        } else {
            // 0 = Ascending, 1 = Descending
            SortDirection vaadiDirection = sortOrders.get(0).getDirection();
            String col = sortOrders.get(0).getSorted();
            int sortDir = vaadiDirection == SortDirection.ASCENDING ? org.apache.jena.query.Query.ORDER_ASCENDING
                    : org.apache.jena.query.Query.ORDER_DESCENDING;
            System.out.println(sortDir);
            Path path = Path.newPath();
            if (col.isEmpty()) {
                path.fwd("http://www.example.org/" + col);
                // lol?
                System.out.println("is empty");
            }
            dataQuery.addOrderBy(new NodePathletPath(path), sortDir);
            limit += limit;
        }
        List<T> list = dataQuery
                .limit(limit)
                .offset(query.getOffset())
                .exec()
                .toList()
                .blockingGet();
        list.forEach(System.out::println);
        list.forEach(System.out::println);
        Stream<T> stream = list.stream();
        return stream;
    }
}
