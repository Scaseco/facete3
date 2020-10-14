package org.aksw.facete3.app.vaadin.components;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.providers.SearchProvider;

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class SearchComponent extends VerticalLayout {

    private static final long serialVersionUID = -331380480912293631L;
    protected MainView mainView;
    protected SearchProvider searchProvider;

    public SearchComponent(MainView mainView, SearchProvider searchProvider) {
        this.mainView = mainView;
        this.searchProvider = searchProvider;
        
        addSearchComponent();
    }


    private void addSearchComponent() {
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search for Papers...");
        searchField.addValueChangeListener(this::searchCallback);
        add(searchField);
    }

    private void searchCallback(ComponentValueChangeEvent<TextField, String> event) {
        String query = event.getValue();
        RDFNodeSpec searchResult = searchProvider.search(query);
//        URI uri = UriComponentsBuilder.fromUriString(nliConfig.getEnpoint())
//                .queryParam("query", query)
//                .queryParam("limit", nliConfig.getResultLimit())
//                .build()
//                .toUri();
//        NliResponse response = restTemplate.getForObject(uri, NliResponse.class);
        mainView.handleSearchResponse(searchResult);
    }
}
