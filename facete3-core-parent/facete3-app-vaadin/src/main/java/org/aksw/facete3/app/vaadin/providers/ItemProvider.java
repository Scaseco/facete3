package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;
import com.vaadin.flow.data.provider.Query;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public class ItemProvider extends FacetProvider<RDFNode> {

    private static final long serialVersionUID = 587055871703757617L;

    public ItemProvider(QueryConf queryConf) {
        super(queryConf);
    }

    @Override
    protected DataQuery<RDFNode> translateQuery(Query<RDFNode, Void> query) {
        DataQuery<RDFNode> dataQuery = queryConf.getFacetedQuery()
                .focus()
                .availableValues();
        return dataQuery;
    }

    @Override
    protected Function<? super RDFNode, ? extends Node> getNodeFunction() {
      return RDFNode::asNode;
    }
}
