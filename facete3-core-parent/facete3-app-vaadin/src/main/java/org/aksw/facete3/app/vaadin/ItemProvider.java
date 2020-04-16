package org.aksw.facete3.app.vaadin;

import com.vaadin.flow.data.provider.Query;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.rdf.model.RDFNode;

public class ItemProvider extends FacetsProvider<RDFNode> {

    private static final long serialVersionUID = 14L;

    public ItemProvider(QueryConf queryConf) {
        super(queryConf);
    }

    @Override
    protected DataQuery<RDFNode> translateQuery(Query<RDFNode, String> query) {
        DataQuery<RDFNode> dataQuery =
                queryConf.getFacetedQuery().focus().availableValues();
        return dataQuery;
    }

    // @Override
    // public Object getId(FacetValueCount facetValueCount){
    //     return facetValueCount.toString().hashCode();
    // }
}