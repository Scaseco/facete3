package org.aksw.facete3.app.vaadin.components;

//public class SearchPluginForm extends FormLayout {
//    protected ComboBox<ServiceStatus> serviceUrl = new ComboBox<>();
////    protected TextField lastName =
////            new TextField("Last name");
////    private ComboBox<Gender> gender =
////            new ComboBox<>("Gender");
//
//    public ComboBox<ServiceStatus> getServiceUrl() {
//        return serviceUrl;
//    }
//
//    public SearchPluginForm() {
//
//        setResponsiveSteps(
//                   new ResponsiveStep("25em", 1),
//                   new ResponsiveStep("32em", 2),
//                   new ResponsiveStep("40em", 3));
//
//        serviceUrl.setDataProvider(new ListDataProvider() {
//            @Override
//            protected void applyFilter(DataQuery<ServiceStatus> dataQuery, String filterText) {
//                UnaryRelation filter = KeywordSearchUtils.createConceptRegexLabelOnly(
//                        BinaryRelationImpl.create(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint")), filterText);
//                dataQuery.filter(filter);
//            }
//
//            @Override
//            protected DataQuery<ServiceStatus> getDataQuery() {
//                DataQuery<ServiceStatus> dq = FacetedQueryBuilder.builder()
//                        .configDataConnection()
//                            .setSource(model)
//                        .end()
//                        .create()
//                        .baseConcept(ConceptUtils.createForRdfType("http://www.w3.org/ns/sparql-service-description#Service"))
//                        .focus()
//                        .availableValues()
//                        .as(ServiceStatus.class)
//                        .add(ResourceFactory.createProperty("http://www.w3.org/ns/sparql-service-description#endpoint"))
//                        .addOptional(ResourceFactory.createProperty("https://schema.org/serverStatus"))
//                        .addOptional(ResourceFactory.createProperty("https://schema.org/dateModified"))
//                        ;
//                return dq;
//            }
//        });
//
//        serviceUrl.setItemLabelGenerator(s -> Optional.ofNullable(s.getEndpoint()).orElse("(null)"));
//        serviceUrl.setRenderer(new ComponentRenderer<>(serviceStatus -> {
//            HorizontalLayout layout = new HorizontalLayout();
//
//            XSDDateTime timestamp = serviceStatus.getDateModified();
//            String timeAgo = timestamp == null
//                    ? "-"
//                    : TimeAgo.toDuration(ChronoUnit.MILLIS.between(
//                            timestamp.asCalendar().toInstant(), Instant.now()),
//                            TimeAgo::fmtCompact);
//
//            String statusStr = Optional.ofNullable(serviceStatus.getServerStatus()).orElse("").toLowerCase();
//
//            // Ternary state: online? yes/no/unknown (null)
//            Boolean isOnline = statusStr == null
//                    ? null
//                    : statusStr.contains("online")
//                        ? Boolean.TRUE
//                        : statusStr.contains("offline")
//                            ? Boolean.FALSE
//                            : null;
//
//            String endpointStr = Optional.ofNullable(serviceStatus.getEndpoint()).orElse("(null)");
//
//            // Note: Marking potentially offline endpoints as grey looks better than in rede
//            // Also, the endpoint may be online after all; the server state is just an indicator
//            String styleStr = isOnline == null
//                    ? "var(--lumo-tertiary-text-color)"
//                    : isOnline
//                        ? "var(--lumo-success-text-color)"
//                        : "var(--lumo-tertiary-text-color)";
//                        //: "var(--lumo-error-text-color)";
//
//            Icon icon = VaadinIcon.CIRCLE.create();
//            icon.getStyle()
//                .set("color", styleStr)
//                .set("width", "1em")
//                .set("height", "1em");
//
//            Span urlSpan = new Span(endpointStr);
//
//            Span ago = new Span(timeAgo);
//            ago.addClassName("detail-text");
//
//            layout.add(icon);
//            layout.add(urlSpan);
//            layout.add(ago);
//
//            layout.setFlexGrow(1, urlSpan);
//
//            return layout;
//        }));
//
//        FormItem formItem = addFormItem(serviceUrl, "Sparql Endpoint URL");
//        serviceUrl.setWidthFull();
//        setColspan(formItem, 3);
////        FormItem formItem = new FormItem();
////        Label label = new Label("Sparql Endpoint URL");
////        label.getElement().setAttribute("slot", "label");
////        formItem.add(label);
////        formItem.add(serviceUrl);
////        add(formItem, 3);
//
////        add(serviceUrl, 3);
//
////        DataRefSparqlEndpoint bean = ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class);
////        bean.setServiceUrl("http://");
//
////        Binder<PlainDataRefSparqlEndpoint> binder = new Binder<>(PlainDataRefSparqlEndpoint.class);
////        binder.setBean(bean);
////        binder.bindInstanceFields(this);
//    }
//}

