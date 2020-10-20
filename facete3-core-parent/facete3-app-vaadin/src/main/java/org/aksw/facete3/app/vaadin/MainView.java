package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.components.ExplorerTabs;
import org.aksw.facete3.app.vaadin.plugin.ComponentPlugin;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("")
@PWA(name = "Vaadin Application", shortName = "Vaadin App",
        description = "This is an example Vaadin application.", enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
@CssImport(value = "./styles/vaadin-grid-styles.css", themeFor = "vaadin-grid")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@Theme(Lumo.class)
public class MainView extends AppLayout {

    // Ensure Jena plugins are fully loaded before
    // beans are passed to components
//    static { JenaSystem.init(); }



    protected static final long serialVersionUID = 7851055480070074549L;
//    protected Config config;


    @Autowired
    public MainView(Config config) {
//        VaadinSession.getCurrent().setErrorHandler(eh -> {
//            Notification.show(ExceptionUtils.getRootCauseMessage(eh.getThrowable()));
//        });

        Facete3Wrapper.initJena();

        HorizontalLayout navbarLayout = new HorizontalLayout();
        navbarLayout.setWidthFull();
        navbarLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button appSettingsBtn = new Button(new Icon(VaadinIcon.COG));
        navbarLayout.add(appSettingsBtn);

//        Span item = new Span();
//        Span icon = new Span();
//        icon.addClassName("color-swatch");
//        icon.getStyle().set("background-color", "var(--lumo-success-text-color)");
//
//        icon.setText("");
//        item.add(icon);
//        item.add("online");
//        //item.add(new Span("online"));
//
////        icon.getStyle().set("background-color", "var(--lumo-success-text-color)");
//        navbarLayout.add(item);
//
//        Dialog dialog = new Dialog();
//        SparqlEndpointForm input = new SparqlEndpointForm();
//
//        dialog.add(input);
//
//        appSettingsBtn.addClickListener(event -> {
//            dialog.open();
////            input.focus();
//        });

        addToNavbar(navbarLayout);
        setContent(getAppContent(config));
    }

    Component getAppContent(Config config) {
        ComponentPlugin plugin = ComponentPlugin.createWithDefaultBase(
                appBuilder -> appBuilder
                .parent(config.context)
                .sources(ConfigRefresh.class)
                //.sources(ConfigNli.class)
                .sources(ConfigSearchProviderSparql.class)
                .sources(ConfigFacetedBrowserViewCord.class));

        VerticalLayout appContent = new VerticalLayout();
        ExplorerTabs tabs = new ExplorerTabs(plugin::newComponent);
        tabs.newTab();
        tabs.setWidthFull();
        appContent.add(tabs);
        return appContent;
    }

}