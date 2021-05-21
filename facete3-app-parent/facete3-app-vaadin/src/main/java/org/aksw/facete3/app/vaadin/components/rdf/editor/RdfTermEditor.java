package org.aksw.facete3.app.vaadin.components.rdf.editor;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.facete3.app.vaadin.providers.DataProviderFromDataQuerySupplier;
import org.aksw.facete3.app.vaadin.util.DataProviderUtils;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterables;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;

enum LiteralMode { LANG, DTYPE };
enum RdfTermType { IRI, BNODE, LITERAL };

interface RdfTermModel {
    RdfTermType getRdfTermType();
    String getLexicalForm();
    String getDatatype();
    String getLanguageTag();

    RdfTermModel setRdfTermType(RdfTermType rdfTermType);

    // Runnable addChangeListener(BiConsumer<? super RdfTermModel, ? super RdfTermModel> beforeAndAfetr);
}


//interface LiteralEditor {
//
//    Runnable install(Component component);
//
//    Node getNode();
//    void setNode(Node node);
//}
//
//
//class LiteralEditorDefault
//	implements LiteralEditor
//{
//
//}
//
//
//class LiteralEditorLangString
//	implements LiteralEditor
//{
//
//}



public class RdfTermEditor
    // extends FormItem
	extends HorizontalLayout
    implements HasValue<ValueChangeEvent<Node>, Node>, HasValueChangeMode
{

    /*
     * GUI Components for editing all aspects of RDF terms
     */

    protected Button iriToggle;
    protected Button bnodeToggle;
    protected Button literalToggle;
	protected Select<RdfTermType> termTypeSelect;
	
    protected TextField resourceTextField;
    protected TextArea literalTextArea;

    protected Button langOrDtypeToggle;

    protected ComboBox<Resource> literalTypeComboBox;
    protected ComboBox<Resource> langComboBox;


    /*
     * Other state
     */

    protected Map<RdfTermType, Icon> rdfTermTypeToIcon = new EnumMap<>(RdfTermType.class);
    protected EnumSet<RdfTermType> allowedRdfTermTypes = EnumSet.allOf(RdfTermType.class);

    //protected RdfTermModel model;
    protected Node value;

    protected Set<ValueChangeListener<? super ValueChangeEvent<Node>>> valueChangeListerens = new LinkedHashSet<>();

//    protected RdfTermType rdfTermType = RdfTermType.IRI;
    protected LiteralMode literalMode = LiteralMode.LANG;

    protected Model typeModel = createTypeModel();
    protected Model langModel = ModelFactory.createDefaultModel();

    public static Model createTypeModel() {

        Model result = ModelFactory.createDefaultModel();

        PrefixMapping prefixMapping = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
        result.setNsPrefixes(prefixMapping);

        TypeMapper typeMapper = TypeMapper.getInstance();
        typeMapper.listTypes().forEachRemaining(type -> {
            String iri = type.getURI();
            if (iri != null) {
                result.getResource(iri)
                    .addProperty(RDF.type, OWL.Thing);
            }
        });

        return result;
    }

    public void init() {
        langComboBox.setItemLabelGenerator(s -> ResourceUtils.tryGetLiteralPropertyValue(s, RDFS.label, String.class).orElse("(null)"));
        langComboBox.setAllowCustomValue(true);
        // langComboBox.
        langComboBox.addCustomValueSetListener(event -> {
            event.getSource().setValue(getOrCreateLang(event.getDetail(), langModel));
        });
        langComboBox.setDataProvider(DataProviderUtils.wrapWithErrorHandler(new DataProviderFromDataQuerySupplier<Resource>() {
            @Override
            protected void applyFilter(DataQuery<Resource> dataQuery, String filterText) {
                UnaryRelation filter = KeywordSearchUtils.createConceptExistsRegexIncludeSubject(
                        BinaryRelationImpl.create(RDFS.label), filterText);
                dataQuery.filter(filter);
            }

            @Override
            protected DataQuery<Resource> getDataQuery() {
                DataQuery<Resource> dq = FacetedQueryBuilder.builder()
                        .configDataConnection()
                            .setSource(langModel)
                        .end()
                        .create()
                        //.baseConcept(ConceptUtils.createForRdfType(OWL..getURI()))
                        .focus()
                        .availableValues()
                        .as(Resource.class)
                        ;
                return dq;
            }
        }));

        literalTypeComboBox.setItemLabelGenerator(s -> Optional.ofNullable(s.getURI()).orElse("(null)"));
        literalTypeComboBox.setAllowCustomValue(true);
        literalTypeComboBox.addCustomValueSetListener(event -> {
            event.getSource().setValue(getOrCreateRdfDatatype(event.getDetail(), typeModel));
        });
        literalTypeComboBox.setDataProvider(DataProviderUtils.wrapWithErrorHandler(new DataProviderFromDataQuerySupplier<Resource>() {
            @Override
            protected void applyFilter(DataQuery<Resource> dataQuery, String filterText) {
                UnaryRelation filter = KeywordSearchUtils.createConceptExistsRegexIncludeSubject(
                        BinaryRelationImpl.create(RDFS.label), filterText);
                dataQuery.filter(filter);
            }

            @Override
            protected DataQuery<Resource> getDataQuery() {
                DataQuery<Resource> dq = FacetedQueryBuilder.builder()
                        .configDataConnection()
                            .setSource(typeModel)
                        .end()
                        .create()
                        .baseConcept(ConceptUtils.createForRdfType(OWL.Thing.getURI()))
                        .focus()
                        .availableValues()
                        .as(Resource.class)
                        ;
                return dq;
            }
        }));

    }
    
    public void redraw() {

        iriToggle.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        bnodeToggle.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
        literalToggle.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);


//        if (this == langOrDtypeToggle.getParent().orElse(null)) {
//            this.remove(langOrDtypeToggle);
//        }
//
//        if (this == langOrDtypeTextArea.getParent().orElse(null)) {
//            this.remove(langOrDtypeTextArea);
//        }


//        remove(langOrDtypeToggle, langOrDtypeTextArea);
        resourceTextField.setVisible(false);
        literalTypeComboBox.setVisible(false);
        langOrDtypeToggle.setVisible(false);
        literalTextArea.setVisible(false);
        literalTypeComboBox.setVisible(false);
        langComboBox.setVisible(false);

        // langOrDtypeTextField.setVisible(false);

        // termTypeSelect.setValue(rdfTermType);
        RdfTermType rdfTermType = getRdfTermType();
        
        switch (rdfTermType) {
        case IRI:
            iriToggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            resourceTextField.setVisible(true);            
            break;
        case BNODE:
            bnodeToggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            resourceTextField.setVisible(true);
            break;
        case LITERAL:
            literalToggle.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            literalTextArea.setVisible(true);

            switch (literalMode) {
            case LANG:
                langOrDtypeToggle.setIcon(new Icon(VaadinIcon.AT));
                langComboBox.setVisible(true);
                break;
            case DTYPE:
                langOrDtypeToggle.setIcon(new Icon(VaadinIcon.FILE_TREE_SUB));
                literalTypeComboBox.setVisible(true);
//                langTextField.setVisible(true);
                break;
            default:
                throw new IllegalStateException("Unknown litral mode: " + literalMode);
            }

            langOrDtypeToggle.setVisible(true);


//            add(langOrDtypeToggle, langOrDtypeTextArea);
            break;
        default:
            throw new IllegalStateException("Unknown rdf term type: " + rdfTermType);
        }

    }


    public static Resource getOrCreateRdfDatatype(String dtype, Model model) {
        Resource result = model.createResource(dtype);
        return result;
    }

    public static Resource getOrCreateLang(String langTag, Model model) {

        List<Resource> cands = model.listSubjectsWithProperty(RDFS.label, langTag).toList();

        Resource result;
        if (cands.isEmpty()) {
            result = ModelFactory.createDefaultModel().createResource()
                .addProperty(RDFS.label, langTag);
        } else {
            result = Iterables.getOnlyElement(cands);
        }

        return result;
    }

    public void nodeToState(Node node) {
        if (node.isURI()) {
            setRdfTermType(RdfTermType.IRI);
            resourceTextField.setValue(node.getURI());
        } else if (node.isBlank()) {
        	setRdfTermType(RdfTermType.BNODE);
            resourceTextField.setValue(node.getBlankNodeLabel());
        } else if (node.isLiteral()) {
        	setRdfTermType(RdfTermType.LITERAL);

            LiteralMode literalMode = NodeUtils.isLangString(node)
                ? LiteralMode.LANG
                : LiteralMode.DTYPE;

            setLiteralMode(literalMode);
            
            String lex = node.getLiteralLexicalForm();

            literalTextArea.setValue(lex);

            switch (literalMode) {
            case LANG:
                String langStr = node.getLiteralLanguage();
                Resource langRes = getOrCreateLang(langStr, langModel);
                langComboBox.setValue(langRes);
                break;
            case DTYPE:
                String dtype = node.getLiteralDatatypeURI();
                Resource r = dtype == null ? null : typeModel.getResource(dtype);
                literalTypeComboBox.setValue(r);
                break;
            default:
                throw new IllegalStateException("Unknown literal mode: " + literalMode);
            }

        } else {
            throw new RuntimeException("Unkown node type: " + node);
        }        
    }

    public Node calcValueFromUiState() {
    	RdfTermType rdfTermType = termTypeSelect.getValue();

        Node result;
        switch (rdfTermType) {
        case IRI:
            result = NodeFactory.createURI(resourceTextField.getValue());
            break;
        case BNODE:
            result = NodeFactory.createBlankNode(resourceTextField.getValue());
            break;
        case LITERAL:
            String lex = literalTextArea.getValue();
            switch (literalMode) {
            case LANG:
                Resource r = langComboBox.getValue();
                String langStr = r == null ? null : ResourceUtils.getLiteralPropertyValue(r, RDFS.label, String.class);
                langStr = langStr == null ? "" : langStr;

                result = NodeFactory.createLiteral(lex, langStr);
                break;
            case DTYPE:
                Resource tmp = literalTypeComboBox.getValue();

                if (tmp != null) {
                    String dtypeIri = tmp.getURI();
                    RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(dtypeIri);
                    result = NodeFactory.createLiteral(lex, dtype);
                } else {
                    result = NodeFactory.createLiteral(lex);
                }
                break;
            default:
                throw new IllegalStateException("Unknown litral mode: " + literalMode);
            }
            break;
        default:
            throw new IllegalStateException("Unknown rdf term type: " + rdfTermType);
        }

        return result;
    }


    public RdfTermEditor() {
//        rdfTermTypeToIcon.put(RdfTermType.IRI, VaadinIcon.CODE);
//        rdfTermTypeToIcon.put(RdfTermType.IRI, VaadinIcon.CODE);
//        rdfTermTypeToIcon.put(RdfTermType.IRI, VaadinIcon.CODE);

        iriToggle = new Button(new Icon(VaadinIcon.CODE));
        bnodeToggle = new Button(new Icon(VaadinIcon.CIRCLE_THIN));
        literalToggle = new Button(new Icon(VaadinIcon.FILE_TEXT_O));

        termTypeSelect = new Select<>();
        termTypeSelect.setItems(RdfTermType.IRI, RdfTermType.LITERAL, RdfTermType.BNODE);
        termTypeSelect.setValue(RdfTermType.IRI);
        termTypeSelect.setRenderer(new ComponentRenderer<>(item -> {
        	Component r;
        	switch(item) {
        	case IRI: r = new Icon(VaadinIcon.CODE); break;
        	case LITERAL: r = new Icon(VaadinIcon.FILE_TEXT_O); break;
        	case BNODE: r = new Icon(VaadinIcon.CIRCLE_THIN); break;
        	default: r = new Span(Objects.toString(item)); break;
        	}
        	return r;
        }));
        
        resourceTextField = new TextField();
//        resourceTextField.setWidthFull();

        
        literalTextArea = new TextArea();
//        literalTextArea.setWidthFull();

        langOrDtypeToggle = new Button(new Icon(VaadinIcon.AT)); // VaadinIcon.CHAT

        literalTypeComboBox = new ComboBox<Resource>();
//        literalTypeComboBox.setWidthFull();

        langComboBox = new ComboBox<Resource>();
//        literalTypeComboBox.setWidthFull();

        langOrDtypeToggle.addClickListener(event -> {
            literalMode = LiteralMode.DTYPE.equals(literalMode)
                    ? LiteralMode.LANG
                    : LiteralMode.DTYPE;

            redraw();
        });

        //langTextField = new TextField();
//        langOrDtypeTextField = new TextField();

//        termTypeSelect.addValueChangeListener(vce -> {
//        	rdfTermType = vce.getHasValue().getValue();
//            calcValueFromUiState();
//            redraw();
//        });

        iriToggle.addClickListener(event -> {
            setRdfTermType(RdfTermType.IRI);
            calcValueFromUiState();
            redraw();
        });

        bnodeToggle.addClickListener(event -> {
        	setRdfTermType(RdfTermType.BNODE);
            calcValueFromUiState();
            redraw();
        });

        literalToggle.addClickListener(event -> {
        	setRdfTermType(RdfTermType.LITERAL);
            calcValueFromUiState();
            redraw();
        });

        addToComponent(this);

        init();
        redraw();

        setValueChangeMode(ValueChangeMode.LAZY);
        registerEventListeners();
    }
    

    public void setRdfTermType(RdfTermType termType) {
    	termTypeSelect.setValue(termType);
    }
    
    public RdfTermType getRdfTermType() {
    	return termTypeSelect.getValue();
    }

    
    public void setLiteralMode(LiteralMode literalMode) {
		this.literalMode = literalMode;
	}
    
    public LiteralMode getLiteralMode() {
		return literalMode;
	}

    public Component[] getAllComponents() {
        return new Component[] {iriToggle, bnodeToggle, literalToggle, resourceTextField, literalTextArea, langOrDtypeToggle, literalTypeComboBox, langComboBox };
    }

    public void addToComponent(HasComponents target) {
    	HorizontalLayout tmp = this; //new HorizontalLayout();
    	tmp.setWidthFull();

        // tmp.add(iriToggle, bnodeToggle, literalToggle, resourceTextField, literalTextArea, langOrDtypeToggle, literalTypeComboBox, langComboBox);
        tmp.add(termTypeSelect, resourceTextField, literalTextArea, langOrDtypeToggle, literalTypeComboBox, langComboBox);
        tmp.setFlexGrow(0, termTypeSelect);
        tmp.setFlexGrow(2, resourceTextField, literalTextArea);
        tmp.setFlexGrow(1, literalTypeComboBox, langComboBox);

    	// target.add(tmp);

        // target.add(iriToggle, bnodeToggle, literalToggle, resourceTextField, literalTextArea, langOrDtypeToggle, literalTypeComboBox, langComboBox);
    }

    protected void handleEvent(ValueChangeEvent<?> event) {
        Node node = calcValueFromUiState();
        setValue(node);
    }

    protected Registration registerEventListeners() {

        List<Registration> registrations = Arrays.asList(
        		termTypeSelect.addValueChangeListener(this::handleEvent),
                resourceTextField.addValueChangeListener(this::handleEvent),
                literalTextArea.addValueChangeListener(this::handleEvent),
                literalTypeComboBox.addValueChangeListener(this::handleEvent),
                langComboBox.addValueChangeListener(this::handleEvent));

        return () -> registrations.forEach(Registration::remove);
    }


    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<Node>> listener) {
        valueChangeListerens.add(listener);
        return () -> valueChangeListerens.remove(listener);
    }



    @Override
    public void setValue(Node value) {
        Node oldValue = this.value;
        if (!Objects.equals(oldValue, value)) {
            this.value = value;

            valueChangeListerens.forEach(listener ->
                listener.valueChanged(new ComponentValueChangeEvent<>(this, this, oldValue, false)));

            nodeToState(value);
            redraw();
        }
    }

    @Override
    public Node getValue() {
        return value;
    }


    @Override
    public void setReadOnly(boolean readOnly) {
        // TODO Implement
    }

    @Override
    public boolean isReadOnly() {
        // TODO Implement
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // TODO Implement
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        // TODO Implement
        return false;
    }


    protected ValueChangeMode valueChangeMode;

    @Override
    public ValueChangeMode getValueChangeMode() {
        return valueChangeMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        for (Component component : getAllComponents()) {
            if (component instanceof HasValueChangeMode) {
                ((HasValueChangeMode)component).setValueChangeMode(valueChangeMode);
            }
        }
    }



}
