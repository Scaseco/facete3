package org.aksw.facete3.app.vaadin.components.rdf.tablemapper;

import org.aksw.jenax.sparql.relation.api.UnaryRelation;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;

/** A predicate tree's node types alternate between 'subject' and 'predicate'. The root is always a subject node. */
class Node {

}

class SubjectNode
    extends Node
{
    protected UnaryRelation setSpecification;

    public UnaryRelation getSetSpecification() {
        return setSpecification;
    }
}

class PredicateNode
    extends Node
{

}


/**
 * Options for how to derive a set of predicates from a given set of subjects.
 *
 * Specifically:
 * <ul>
 *   <li>Forward/Backward property switch</li>
 *   <li>Sample options for how to derive a set of properties</li>
 *   <li>Filter over the property name</li>
 *   <li>Pagination</li>
 *   <li>Declaration of virtual predicates at this point in the tree</li>
 * </ul>
 *
 */
class SubjectDetailsView
    extends FormLayout
{
    protected Checkbox isForward;
    // TODO Controls for sampling the set of resources for which to derive the predicates
    protected TextField filterField;

    // TODO Slider for which predicates to retrieve

    public SubjectDetailsView() {
        isForward = new Checkbox();
        filterField = new TextField();

        addFormItem(isForward, "show forward predicates");
        addFormItem(filterField, "Filter predicates");
    }
}


/**
 * Options for how to derive a set of predicates from a given set of predicates.
 *
 * Specifically:
 * <ul>
 *   <li>Which columns this predicate is linked to - in SQL this would be how many aliases exist of that predicate</li>
 * </ul>
 *
 */
class PredicateDetailsView {

}


public class TableMapperComponent
    extends HorizontalLayout
{
    protected TreeGrid<Node> treeGrid = new TreeGrid<>();

    protected SubjectDetailsView subjectDetailsView = new SubjectDetailsView();
    protected PredicateDetailsView predicateDetailsView = new PredicateDetailsView();


    public TableMapperComponent() {
        initComponent();
    }

    public void initComponent() {
        setWidthFull();

        treeGrid.addComponentHierarchyColumn(person -> {
//            Avatar avatar = new Avatar();
//            avatar.setName(person.getFullName());
//            avatar.setImage(person.getPictureUrl());
//
//            Span fullName = new Span(person.getFullName());
//
//            Span profession = new Span(person.getProfession());
//            profession.getStyle()
//                    .set("color", "var(--lumo-secondary-text-color)")
//                    .set("font-size", "var(--lumo-font-size-s)");
//
//            VerticalLayout column = new VerticalLayout(fullName, profession);
//            column.getStyle().set("line-height", "var(--lumo-line-height-m)");
//            column.setPadding(false);
//            column.setSpacing(false);
//
            HorizontalLayout row = new HorizontalLayout(); //avatar, column);
//            row.setAlignItems(FlexComponent.Alignment.CENTER);
//            row.setSpacing(true);
            return row;
        }).setHeader("Data Tree");

        treeGrid.addComponentColumn(person -> {
//            Icon emailIcon = createIcon(VaadinIcon.ENVELOPE);
//            Span email = new Span(person.getEmail());
//
//            Anchor emailLink = new Anchor();
//            emailLink.add(emailIcon, email);
//            emailLink.setHref("mailto:" + person.getEmail());
//            emailLink.getStyle().set("align-items", "center").set("display",
//                    "flex");
//
//            Icon phoneIcon = createIcon(VaadinIcon.PHONE);
//            Span phone = new Span(person.getAddress().getPhone());
//
//            Anchor phoneLink = new Anchor();
//            phoneLink.add(phoneIcon, phone);
//            phoneLink.setHref("tel:" + person.getAddress().getPhone());
//            phoneLink.getStyle().set("align-items", "center").set("display",
//                    "flex");

            VerticalLayout column = new VerticalLayout();
//            column.getStyle().set("font-size", "var(--lumo-font-size-s)")
//                    .set("line-height", "var(--lumo-line-height-m)");
//            column.setPadding(false);
//            column.setSpacing(false);
            return column;
        }).setHeader("Extra");

        add(treeGrid);
    }
}
