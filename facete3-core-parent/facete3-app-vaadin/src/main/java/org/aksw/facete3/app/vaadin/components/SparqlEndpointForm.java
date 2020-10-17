package org.aksw.facete3.app.vaadin.components;

import java.util.Optional;

import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.facete3.app.vaadin.ServiceStatus;
import org.aksw.facete3.app.vaadin.providers.DataProviderFromDataQuerySupplier;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class SparqlEndpointForm extends FormLayout {
    protected ComboBox<ServiceStatus> serviceUrl = new ComboBox<>();
//    protected TextField lastName =
//            new TextField("Last name");
//    private ComboBox<Gender> gender =
//            new ComboBox<>("Gender");

    public SparqlEndpointForm() {

        Model model = RDFDataMgr.loadModel("https://raw.githubusercontent.com/SmartDataAnalytics/lodservatory/master/latest-status.ttl");

        serviceUrl.setWidth("100em");
        serviceUrl.addCustomValueSetListener(
                event -> {
                    String value = event.getDetail();
                    if (value != null) {
                        String iri = value + "#service";

                        ServiceStatus bean = ModelFactory.createDefaultModel().createResource(iri).as(ServiceStatus.class);
                        bean.setEndpoint(value).setServerStatus("http://unknown");

                        serviceUrl.setValue(bean);
                    }
                });

        serviceUrl.setDataProvider(new DataProviderFromDataQuerySupplier<ServiceStatus>() {
            @Override
            protected void applyFilter(DataQuery<ServiceStatus> dataQuery, String filterText) {
                UnaryRelation filter = KeywordSearchUtils.createConceptRegexLabelOnly(
                        BinaryRelationImpl.create(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint")), filterText);
                dataQuery.filter(filter);
            }

            @Override
            protected DataQuery<ServiceStatus> getDataQuery() {
                DataQuery<ServiceStatus> dq = FacetedQueryBuilder.builder()
                        .configDataConnection()
                            .setSource(model)
                        .end()
                        .create()
                        .baseConcept(ConceptUtils.createForRdfType("http://www.w3.org/ns/sparql-service-description#Service"))
                        .focus()
                        .availableValues()
                        .as(ServiceStatus.class)
                        .add(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint"))
                        .addOptional(ResourceFactory.createProperty("https://schema.org/serverStatus"))
                        ;
                return dq;
            }
        });

        serviceUrl.setItemLabelGenerator(s -> Optional.ofNullable(s.getEndpoint()).orElse("(null)"));
        serviceUrl.setRenderer(new ComponentRenderer<>(serviceStatus -> {
            Div div = new Div();
            div.add(Optional.ofNullable(serviceStatus.getEndpoint()).orElse("(null)"));
            div.add(Optional.ofNullable(serviceStatus.getServerStatus()).orElse("(null)"));
            return div;
        }));

        add(serviceUrl);

//        DataRefSparqlEndpoint x;
//        DataRefSparqlEndpoint x;
        DataRefSparqlEndpoint bean = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class);
        bean.setServiceUrl("http://");

//        Binder<DataRefSparqlEndpoint> binder = new Binder<>(DataRefSparqlEndpoint.class);
//        binder.setBean(bean);
//        binder.bindInstanceFields(this);

        setSizeFull();
    }
}

