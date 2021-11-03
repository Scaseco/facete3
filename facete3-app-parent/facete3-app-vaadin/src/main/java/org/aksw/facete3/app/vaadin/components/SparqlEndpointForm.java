package org.aksw.facete3.app.vaadin.components;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.facete3.app.shared.time.TimeAgo;
import org.aksw.facete3.app.vaadin.ServiceStatus;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderFromDataQuerySupplier;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public class SparqlEndpointForm extends FormLayout {
    protected ComboBox<ServiceStatus> serviceUrl = new ComboBox<>();
//    protected TextField lastName =
//            new TextField("Last name");
//    private ComboBox<Gender> gender =
//            new ComboBox<>("Gender");

    public ComboBox<ServiceStatus> getServiceUrl() {
        return serviceUrl;
    }

    public SparqlEndpointForm() {

        setResponsiveSteps(
                   new ResponsiveStep("25em", 1),
                   new ResponsiveStep("32em", 2),
                   new ResponsiveStep("40em", 3));

        Model model = RDFDataMgr.loadModel("https://raw.githubusercontent.com/SmartDataAnalytics/lodservatory/master/latest-status.ttl");
        RDFDataMgr.read(model, "extra-endpoints.ttl");

        serviceUrl.addCustomValueSetListener(
                event -> {
                    String value = event.getDetail();
                    System.out.println("TRIGGER WITH VALUE " + value);
                    if (value != null) {
                        String iri = value + "#service";

                        ServiceStatus bean = ModelFactory.createDefaultModel().createResource(iri).as(ServiceStatus.class);
                        bean.setEndpoint(value);

                        serviceUrl.setValue(bean);
                    }
                });

        serviceUrl.setDataProvider(new DataProviderFromDataQuerySupplier<ServiceStatus>() {
            @Override
            protected void applyFilter(DataQuery<ServiceStatus> dataQuery, String filterText) {
                UnaryRelation filter = KeywordSearchUtils.createConceptExistsRegexLabelOnly(
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
                        .addOptional(ResourceFactory.createProperty("https://schema.org/dateModified"))
                        ;
                return dq;
            }
        });

        serviceUrl.setItemLabelGenerator(s -> Optional.ofNullable(s.getEndpoint()).orElse("(null)"));
        serviceUrl.setRenderer(new ComponentRenderer<>(serviceStatus -> {
            HorizontalLayout layout = new HorizontalLayout();

            XSDDateTime timestamp = serviceStatus.getDateModified();
            String timeAgo = timestamp == null
                    ? "-"
                    : TimeAgo.toDuration(ChronoUnit.MILLIS.between(
                            timestamp.asCalendar().toInstant(), Instant.now()),
                            TimeAgo::fmtCompact);

            String statusStr = Optional.ofNullable(serviceStatus.getServerStatus()).orElse("").toLowerCase();

            // Ternary state: online? yes/no/unknown (null)
            Boolean isOnline = statusStr == null
                    ? null
                    : statusStr.contains("online")
                        ? Boolean.TRUE
                        : statusStr.contains("offline")
                            ? Boolean.FALSE
                            : null;

            String endpointStr = Optional.ofNullable(serviceStatus.getEndpoint()).orElse("(null)");

            // Note: Marking potentially offline endpoints as grey looks better than in rede
            // Also, the endpoint may be online after all; the server state is just an indicator
            String styleStr = isOnline == null
                    ? "var(--lumo-tertiary-text-color)"
                    : isOnline
                        ? "var(--lumo-success-text-color)"
                        : "var(--lumo-tertiary-text-color)";
                        //: "var(--lumo-error-text-color)";

            Icon icon = VaadinIcon.CIRCLE.create();
            icon.getStyle()
                .set("color", styleStr)
                .set("width", "1em")
                .set("height", "1em");

            Span urlSpan = new Span(endpointStr);

            Span ago = new Span(timeAgo);
            ago.addClassName("detail-text");

            layout.add(icon);
            layout.add(urlSpan);
            layout.add(ago);

            layout.setFlexGrow(1, urlSpan);

            return layout;
        }));

        FormItem formItem = addFormItem(serviceUrl, "Sparql Endpoint URL");
        serviceUrl.setWidthFull();
        setColspan(formItem, 3);
//        FormItem formItem = new FormItem();
//        Label label = new Label("Sparql Endpoint URL");
//        label.getElement().setAttribute("slot", "label");
//        formItem.add(label);
//        formItem.add(serviceUrl);
//        add(formItem, 3);

//        add(serviceUrl, 3);

//        DataRefSparqlEndpoint bean = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class);
//        bean.setServiceUrl("http://");

//        Binder<PlainDataRefSparqlEndpoint> binder = new Binder<>(PlainDataRefSparqlEndpoint.class);
//        binder.setBean(bean);
//        binder.bindInstanceFields(this);
    }
}

