package org.aksw.facete3.app.vaadin.components;

import java.net.URI;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.aksw.facete3.app.vaadin.Config;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.Config.Nli;
import org.aksw.facete3.app.vaadin.domain.NliResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchComponent extends VerticalLayout {

    private static final long serialVersionUID = -331380480912293631L;
    private MainView mainView;
    private RestTemplate restTemplate;
    private Nli nliConfig;

    public SearchComponent(MainView mainView, Config config) {
        this.mainView = mainView;
        nliConfig = config.getNli();
        setRestTemplate();
        addSearchField();
    }

    private void setRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        restTemplate = builder.build();
    }

    public void addSearchField() {
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search for Papers...");
        searchField.addValueChangeListener(this::searchCallback);
        add(searchField);
    }

    private void searchCallback(ComponentValueChangeEvent<TextField, String> event) {
        String query = event.getValue();
        URI uri = UriComponentsBuilder.fromUriString(nliConfig.getEnpoint())
                .queryParam("query", query)
                .queryParam("limit", nliConfig.getResultLimit())
                .build()
                .toUri();
        NliResponse response = restTemplate.getForObject(uri, NliResponse.class);
        mainView.handleNliResponse(response);
    }
}
