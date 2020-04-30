package org.aksw.facete3.app.vaadin.components;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.aksw.facete3.app.vaadin.NliResponse;

public class SearchComponent extends VerticalLayout {

    private static final long serialVersionUID = -331380480912293631L;

    // http://cord19.aksw.org/nli" --data-urlencode "query=$QUERY" --data-urlencode "limit=$LIMIT
    public SearchComponent() {
        WebTarget target = ClientBuilder.newClient()
                .target("http://cord19.aksw.org/nli")
                .queryParam("limit", "15");
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search for Papers...");
        searchField.addValueChangeListener(event -> {
            String query = event.getValue();
            NliResponse test = target.queryParam("query", query)
                    .request(MediaType.APPLICATION_JSON)
                    .get(NliResponse.class);
            test.getResults()
                    .forEach(i -> System.out.println(i.getId()));
        });
        add(searchField);
    }

    public void refresh() {}
}
