package org.aksw.facete3.app.vaadin.components.rdf.editor;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

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

public class RdfTermEditor
    extends FormItem
{
    protected Button iriToggle;
    protected Button bnodeToggle;
    protected Button literalToggle;


    protected TextField resourceTextField;
    protected TextArea literalTextArea;

//    protected Button langToggle;
//    protected Button dtypeToggle;


    protected Button langOrDtypeToggle;
    protected TextField langOrDtypeTextArea;


    protected Map<RdfTermType, Icon> rdfTermTypeToIcon = new EnumMap<>(RdfTermType.class);

    protected EnumSet<RdfTermType> allowedRdfTermTypes = EnumSet.allOf(RdfTermType.class);

    protected RdfTermModel model;


    protected RdfTermType rdfTermType = RdfTermType.IRI;
    protected LiteralMode literalMode = LiteralMode.LANG;

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
        literalTextArea.setVisible(false);
        langOrDtypeToggle.setVisible(false);
        langOrDtypeTextArea.setVisible(false);

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
                break;
            case DTYPE:
                langOrDtypeToggle.setIcon(new Icon(VaadinIcon.FILE_TREE_SUB));
                break;
            default:
                throw new IllegalStateException("Unknown litral mode: " + literalMode);
            }

            langOrDtypeToggle.setVisible(true);
            langOrDtypeTextArea.setVisible(true);
//            add(langOrDtypeToggle, langOrDtypeTextArea);
            break;
        default:
            throw new IllegalStateException("Unknown rdf term type: " + rdfTermType);
        }

    }



    public RdfTermEditor() {
//        rdfTermTypeToIcon.put(RdfTermType.IRI, VaadinIcon.CODE);
//        rdfTermTypeToIcon.put(RdfTermType.IRI, VaadinIcon.CODE);
//        rdfTermTypeToIcon.put(RdfTermType.IRI, VaadinIcon.CODE);

        iriToggle = new Button(new Icon(VaadinIcon.CODE));
        bnodeToggle = new Button(new Icon(VaadinIcon.CIRCLE_THIN));
        literalToggle = new Button(new Icon(VaadinIcon.FILE_TEXT_O));

        resourceTextField = new TextField();
        literalTextArea = new TextArea();

        langOrDtypeToggle = new Button(new Icon(VaadinIcon.AT)); // VaadinIcon.CHAT
//        dtypeToggle = new Button(new Icon(VaadinIcon.FILE_TREE_SUB)); // VaadinIcon.CLUSTER


        langOrDtypeToggle.addClickListener(event -> {
            literalMode = LiteralMode.DTYPE.equals(literalMode)
                    ? LiteralMode.LANG
                    : LiteralMode.DTYPE;

            redraw();
        });

        langOrDtypeTextArea = new TextField();


        iriToggle.addClickListener(event -> {
            rdfTermType = RdfTermType.IRI;
            redraw();
        });

        bnodeToggle.addClickListener(event -> {
            rdfTermType = RdfTermType.BNODE;
            redraw();
        });

        literalToggle.addClickListener(event -> {
            rdfTermType = RdfTermType.LITERAL;
            redraw();
        });

        addToComponent(this);

        redraw();
    }


    public void addToComponent(HasComponents target) {
        target.add(iriToggle, bnodeToggle, literalToggle, resourceTextField, literalTextArea, langOrDtypeToggle, langOrDtypeTextArea);
    }



}
