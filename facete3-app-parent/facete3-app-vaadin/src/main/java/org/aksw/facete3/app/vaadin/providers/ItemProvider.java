package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import com.vaadin.flow.data.provider.Query;

public class ItemProvider extends FacetDataProvider<RDFNode> {
    private static final long serialVersionUID = 1;

    public ItemProvider(Facete3Wrapper facete3, LookupService<Node, String> labelService) {
        super(facete3, labelService);
    }

    @Override
    public DataQuery<RDFNode> translateQuery(Query<RDFNode, Void> query) {
        DataQuery<RDFNode> dataQuery = facete3.getFacetedQuery()
                .focus()
                .availableValues();
        return dataQuery;
    }

    @Override
    protected Function<? super RDFNode, ? extends Node> getNodeForLabelFunction() {
      return RDFNode::asNode;
    }
}
