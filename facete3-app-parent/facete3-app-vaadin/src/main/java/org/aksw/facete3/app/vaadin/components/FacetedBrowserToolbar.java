package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class FacetedBrowserToolbar extends HorizontalLayout {
    public FacetedBrowserToolbar() {
        addClassName("toolbar");
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        //Span title = new Span("Toolbar");

//        Button bold = new Button(new Icon(VaadinIcon.COG));
//        Button italic = new Button(new Icon(VaadinIcon.ITALIC));
//        Button underline = new Button(new Icon(VaadinIcon.UNDERLINE));
//        Button left = new Button(new Icon(VaadinIcon.ALIGN_LEFT));
//        Button center = new Button(new Icon(VaadinIcon.ALIGN_CENTER));
//        Button right = new Button(new Icon(VaadinIcon.ALIGN_RIGHT));
//        Button justify = new Button(new Icon(VaadinIcon.ALIGN_JUSTIFY));
//
//        add(title, bold, italic, underline, left, center, right, justify);
//        add(title);
//        setFlexGrow(1, title);
    }
}
