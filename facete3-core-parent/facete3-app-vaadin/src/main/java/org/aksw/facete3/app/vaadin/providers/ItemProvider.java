package org.aksw.facete3.app.vaadin.providers;

import java.util.function.Function;
import com.vaadin.flow.data.provider.Query;
import org.aksw.facete3.app.vaadin.LabelService;
import org.aksw.facete3.app.vaadin.Facete3Wrapper;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

public class ItemProvider extends FacetProvider<RDFNode> {

    private static final long serialVersionUID = 587055871703757617L;

    public ItemProvider(Facete3Wrapper facete3, LabelService labelService) {
        super(facete3, labelService);
    }

    @Override
    protected DataQuery<RDFNode> translateQuery(Query<RDFNode, Void> query) {
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
