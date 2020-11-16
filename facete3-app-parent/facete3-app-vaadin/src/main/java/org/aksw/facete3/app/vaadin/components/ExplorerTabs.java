package org.aksw.facete3.app.vaadin.components;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;
import org.aksw.facete3.app.vaadin.plugin.ManagedComponentSimple;
import org.aksw.facete3.app.vaadin.plugin.ManagedComponentWrapper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;


/**
 * A tabs component with a 'New tab' button and a configurable action.
 *
 * This class uses an experimental setup where {@link ManagedComponent} is used
 * instead of {@link Component}. The difference is, that the former
 * allows for owning its own spring context which is destroyed when .close() is called.
 *
 * TODO Instead of ManagedComponent it would also be possible to hook into
 * {@link #addAttachListener(com.vaadin.flow.component.ComponentEventListener)}
 * and
 * {@link #addDetachListener(com.vaadin.flow.component.ComponentEventListener)}
 *
 * @author raven
 *
 */
public class ExplorerTabs
    extends Div
{
    private static final long serialVersionUID = 1L;

    protected Tabs tabs = new Tabs();
    protected Div pages = new Div();

    protected Tab currentNewTab;
    protected int tabCounter = 0;

    protected Supplier<? extends ManagedComponent> componentSupplier;

    protected Map<Tab, ManagedComponent> tabsToPages = new HashMap<>();

    protected Tab initialTab;
    protected Supplier<? extends ManagedComponent> initialPageSupplier = () -> new ManagedComponentSimple(new Div());


    // TODO The supplier is not a view-model; there should be some
    // DataProvider that provides the components
    public ExplorerTabs(Supplier<? extends ManagedComponent> componentSupplier) {
        this.componentSupplier = componentSupplier;


        // Initial tab - needed because the new tab button must not be the active tab
        // Apparently Vaadin (at least in version 14) allows for the selected tab to
        // be invisible
        initialTab = new Tab();
        initialTab.setClassName("compact");
        ManagedComponent initialPage = initialPageSupplier.get();
        tabs.add(initialTab);
        pages.add(initialPage.getComponent());
        tabsToPages.put(initialTab, initialPage);
        initialTab.setVisible(false);
        initialPage.getComponent().setVisible(false);


        // New tab button
        {
            currentNewTab = new Tab(new Icon(VaadinIcon.PLUS));
            currentNewTab.setClassName("compact");
            Component page = new VerticalLayout();
            tabs.add(currentNewTab);
            pages.add(page);
            tabsToPages.put(currentNewTab, new ManagedComponentSimple(page));
        }

        tabs.addSelectedChangeListener(ev -> {
            tabsToPages.values().forEach(page -> page.getComponent().setVisible(false));

            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab == currentNewTab) {
                newTab();
            }

            Component selectedPage = tabsToPages.get(tabs.getSelectedTab()).getComponent();
            selectedPage.setVisible(true);
        });
        add(tabs, pages);
    }


    /**
     * Append a new tab instance to the tabs component
     * This method is also invoked when the 'new tab' button is clicked
     *
     */
    public void newTab() {
        Tab selectedTab = currentNewTab;
        ManagedComponent convertingPage = tabsToPages.get(selectedTab);

        selectedTab.removeAll();
        selectedTab.add(new Text("Unnamed tab"));
        Icon icon = VaadinIcon.CLOSE.create();
        icon.getStyle()
            .set("width", "1em")
            .set("height", "1em");

        Button close = new Button(icon, click -> {
            int newTabIdx = tabs.indexOf(currentNewTab);
            int removedTabIdx = tabs.indexOf(selectedTab);

//            if (tabsToPages.size() == 2) {
//                initialTab.setVisible(true);
//                initialPage.getComponent().setVisible(true);
//            }

            // If the last tab is closed then its predecessor becomes the active tab
            // (as the successor is the 'new tab' tab)
            if (removedTabIdx + 1 == newTabIdx) {
                tabs.setSelectedIndex(removedTabIdx - 1);
            }

            destroyTab(selectedTab);
        });

        selectedTab.add(close);

        currentNewTab = new Tab(new Icon(VaadinIcon.PLUS));
        currentNewTab.setClassName("compact");

        // Remove the empty content of the 'New Tab' tab
        pages.remove(convertingPage.getComponent());
        convertingPage.close();

        ManagedComponent convertedContent = newContent();
        tabsToPages.put(selectedTab, convertedContent);
        pages.add(convertedContent.getComponent());

        // Add a fresh 'New Tab' button
        VerticalLayout newPage = new VerticalLayout();
        tabs.add(currentNewTab);
        newPage.setVisible(false);
        tabsToPages.put(currentNewTab, new ManagedComponentSimple(newPage));
        pages.add(newPage);
    }

    protected void destroyTab(Tab tab) {
        ManagedComponent page = tabsToPages.get(tab);
        tabs.remove(tab);
        pages.remove(page.getComponent());
        tabsToPages.remove(tab);

        page.close();
    }

    protected ManagedComponent newContent() {
        return componentSupplier.get();
        // return new Text("Page" + tabCounter++);
    }
}
