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
 * @author raven
 *
 */
public class ExplorerTabs
    extends Div
{
    private static final long serialVersionUID = 1L;

    protected Tab currentNewTab;
    protected int tabCounter = 0;

    protected Supplier<? extends ManagedComponent> componentSupplier;

    // TODO The supplier is not a view-model; there should be some
    // DataProvider that provides the components
    public ExplorerTabs(Supplier<? extends ManagedComponent> componentSupplier) {
        this.componentSupplier = componentSupplier;

        Tabs tabs = new Tabs();

        Map<Tab, ManagedComponent> tabsToPages = new HashMap<>();
        Div pages = new Div();

        // Initial tab - needed because the new tab button must not be the active tab
        {
            Tab tab = new Tab("init");
            // Component page = new VerticalLayout();
            ManagedComponent page = componentSupplier.get();
            tabs.add(tab);
            pages.add(page.getComponent());
            tabsToPages.put(tab, page);
        }


        // New tab button
        {
            currentNewTab = new Tab(new Icon(VaadinIcon.PLUS));
            Component page = new VerticalLayout();
            tabs.add(currentNewTab);
            pages.add(page);
            tabsToPages.put(currentNewTab, new ManagedComponentSimple(page));
        }

        tabs.addSelectedChangeListener(ev -> {
            tabsToPages.values().forEach(page -> page.getComponent().setVisible(false));

            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab == currentNewTab) {
                ManagedComponent convertingPage = tabsToPages.get(selectedTab);
//                VerticalLayout convertingComponent = (VerticalLayout)convertingPage.getComponent();

                selectedTab.removeAll();
                selectedTab.add(new Text("Foo"));
                Button close = new Button(VaadinIcon.CLOSE.create(), click -> {
                    int newTabIdx = tabs.indexOf(currentNewTab);
                    int removedTabIdx = tabs.indexOf(selectedTab);

                    // If the last tab is closed then its predecessor becomes the active tab
                    // (as the successor is the 'new tab' tab)
                    if (removedTabIdx + 1 == newTabIdx) {
                        tabs.setSelectedIndex(removedTabIdx - 1);
                    }

                    ManagedComponent page = tabsToPages.get(selectedTab);
                    tabs.remove(selectedTab);
                    pages.remove(page.getComponent());
                    tabsToPages.remove(selectedTab);

                    page.close();
                });
                selectedTab.add(close);

                currentNewTab = new Tab(new Icon(VaadinIcon.PLUS));
                VerticalLayout newPage = new VerticalLayout();
                tabs.add(currentNewTab);
                newPage.setVisible(false);
                tabsToPages.put(currentNewTab, new ManagedComponentSimple(newPage));
                pages.add(newPage);

                ManagedComponent convertedContent = newContent();


                tabsToPages.put(selectedTab, new ManagedComponentWrapper(convertedContent) {
                    @Override
                    public Component getComponent() {
                        return convertingPage.getComponent();
                    }
                });
                ((VerticalLayout)convertingPage.getComponent()).add(convertedContent.getComponent());
                // pages(convertingPage, substitute);

//                convertingPage.add(new Text("Page" + tabCounter++));
            }

            Component selectedPage = tabsToPages.get(tabs.getSelectedTab()).getComponent();
            selectedPage.setVisible(true);
        });
        add(tabs, pages);
    }

    protected ManagedComponent newContent() {
        return componentSupplier.get();
        // return new Text("Page" + tabCounter++);
    }
}
