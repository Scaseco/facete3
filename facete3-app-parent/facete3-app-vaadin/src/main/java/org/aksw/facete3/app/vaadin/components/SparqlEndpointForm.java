package org.aksw.facete3.app.vaadin.components;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

import org.aksw.commons.util.time.TimeAgo;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.facete3.app.vaadin.ServiceStatus;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuth;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuthBasic;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuthBearerToken;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderFromDataQuerySupplier;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;

public class SparqlEndpointForm extends FormLayout {
    protected ComboBox<ServiceStatus> serviceUrlOld = new ComboBox<>();

    protected TextField serviceUrl = new TextField("Endpoint URL");

    protected Checkbox unionDefaultGraphMode = new Checkbox();
    protected Select<AuthMode> authModeSelect = new Select<>();

    protected TextField usernameInput = new TextField("User Name");
    protected PasswordField passwordInput = new PasswordField("Password");

    protected TextField bearerTokenInput = new TextField("Bearer token");

    protected Multimap<AuthMode, Component> authComponents = ArrayListMultimap.create();

    // public ComboBox<ServiceStatus> getServiceUrl() {
    public String getServiceUrl() {
        return serviceUrl.getValue();
    }

    public AuthMode getAuthMode() {
        return authModeSelect.getValue();
    }

    public String getBearerToken() {
        return bearerTokenInput.getValue();
    }

    public String getPassword() {
        return passwordInput.getValue();
    }

    public String getUsername() {
        return usernameInput.getValue();
    }

    public Checkbox getUnionDefaultGraphMode() {
        return unionDefaultGraphMode;
    }

    public RdfAuth getAuth() {
        AuthMode authMode = getAuthMode();
        RdfAuth result = null;
        switch (authMode) {
        case BASIC:
            RdfAuthBasic basic = ModelFactory.createDefaultModel().createResource().as(RdfAuthBasic.class);
            basic.setUsername(getUsername());
            basic.setPassword(getPassword());
            result = basic;
            break;
        case BEARER_TOKEN:
            RdfAuthBearerToken bearer = ModelFactory.createDefaultModel().createResource().as(RdfAuthBearerToken.class);
            bearer.setBearerToken(getBearerToken());
        default:
        }
        return result;
    }

    public SparqlEndpointForm() {

        setResponsiveSteps(
                   new ResponsiveStep("25em", 1),
                   new ResponsiveStep("32em", 2),
                   new ResponsiveStep("40em", 3));

        Model model = RDFDataMgr.loadModel("https://raw.githubusercontent.com/SmartDataAnalytics/lodservatory/master/latest-status.ttl");

        RDFDataMgr.read(model, "extra-endpoints.ttl");

        serviceUrlOld.addCustomValueSetListener(
                event -> {
                    String value = event.getDetail();
                    System.out.println("TRIGGER WITH VALUE " + value);
                    if (value != null) {
                        String iri = value + "#service";

                        ServiceStatus bean = ModelFactory.createDefaultModel().createResource(iri).as(ServiceStatus.class);
                        bean.setEndpoint(value);

                        serviceUrlOld.setValue(bean);
                    }
                });

        serviceUrlOld.setDataProvider(DataProviderUtils.wrapWithErrorHandler(new DataProviderFromDataQuerySupplier<ServiceStatus>() {
            @Override
            protected void applyFilter(DataQuery<ServiceStatus> dataQuery, String filterText) {
                Node node = NodeFactory.createURI(filterText);
                Fragment1 filter;
                if (!NodeUtils.isValid(node)) {
                    filter = new Concept(new ElementFilter(NodeValue.FALSE), Vars.s);
                } else {
                    filter = KeywordSearchUtils.createConceptExistsStrConstainsLabelOnly(
                            Fragment2Impl.create(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint")), filterText);
                }
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
        }));

        serviceUrlOld.setItemLabelGenerator(s -> Optional.ofNullable(s.getEndpoint()).orElse("(null)"));
        serviceUrlOld.setRenderer(new ComponentRenderer<>(serviceStatus -> {
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


        {
            serviceUrl.setAutocomplete(Autocomplete.URL);
            serviceUrl.getElement().setAttribute("name", "endpointUrl");
            FormItem formItem = addFormItem(serviceUrl, "Sparql Endpoint URL");
            serviceUrl.setWidthFull();
            setColspan(formItem, 3);
        }

        {
            FormItem formItem = addFormItem(unionDefaultGraphMode, "Union default graph");
            unionDefaultGraphMode.setWidthFull();
            setColspan(formItem, 3);
        }

        {
            authModeSelect.setEmptySelectionAllowed(false);
            authModeSelect.setItems(AuthMode.NONE, AuthMode.BASIC, AuthMode.BEARER_TOKEN);
            authModeSelect.setValue(AuthMode.NONE);
            authModeSelect.setTextRenderer(AuthMode::getName);
            FormItem formItem = addFormItem(authModeSelect, "Authentication Type");

            authModeSelect.addValueChangeListener(ev -> {
                updateAuthDialog(ev.getValue());
            });

            bearerTokenInput.setWidthFull();
            setColspan(formItem, 3);
        }

        {
            // https://vaadin.com/forum/thread/17399734/leverage-browser-save-password-feature
            usernameInput.setAutocomplete(Autocomplete.USERNAME);
            usernameInput.getElement().setAttribute("name", "username");
            Element dummy = new Element("input");
            dummy.setAttribute("type", "text");
            dummy.setAttribute("slot", "input");
            usernameInput.getElement().appendChild(dummy);
            FormItem formItem = addFormItem(usernameInput, "User Name");
            usernameInput.setWidthFull();
            setColspan(formItem, 3);
            authComponents.put(AuthMode.BASIC, formItem);
        }

        {
            passwordInput.setAutocomplete(Autocomplete.CURRENT_PASSWORD);
            passwordInput.getElement().setAttribute("name", "current-password");
            Element dummy = new Element("input");
            dummy.setAttribute("type", "text");
            dummy.setAttribute("slot", "input");
            passwordInput.getElement().appendChild(dummy);
            FormItem formItem = addFormItem(passwordInput, "Password");
            passwordInput.setWidthFull();
            setColspan(formItem, 3);
            authComponents.put(AuthMode.BASIC, formItem);
        }

        {
            FormItem formItem = addFormItem(bearerTokenInput, "Bearer token");
            bearerTokenInput.setWidthFull();
            setColspan(formItem, 3);
            authComponents.put(AuthMode.BEARER_TOKEN, formItem);
        }

        updateAuthDialog(AuthMode.NONE);
    }

    public void updateAuthDialog(AuthMode authType) {
        for (Entry<AuthMode, Collection<Component>> e : authComponents.asMap().entrySet()) {
            boolean setVisible = e.getKey().equals(authType);
            for (Component component : e.getValue()) {
                component.setVisible(setVisible);
            }
        }
    }
}
