package org.aksw.facete3.app.vaadin.components;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

public class ExplorerTabs
    extends Div
{
    private static final long serialVersionUID = 1L;

    protected Tab currentNewTab;
    protected int tabCounter = 0;

    public ExplorerTabs() {

        Tabs tabs = new Tabs();

        Map<Tab, Component> tabsToPages = new HashMap<>();
        Div pages = new Div();

        // Initial tab
        {
            Tab tab = new Tab("init");
            Component page = new VerticalLayout();
            tabs.add(tab);
            pages.add(page);
            tabsToPages.put(tab, page);
        }


        // New tab button
        {
            currentNewTab = new Tab(new Icon(VaadinIcon.PLUS));
            Component page = new VerticalLayout();
            tabs.add(currentNewTab);
            pages.add(page);
            tabsToPages.put(currentNewTab, page);
        }

        tabs.addSelectedChangeListener(ev -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));

            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab == currentNewTab) {
                VerticalLayout convertingPage = (VerticalLayout)tabsToPages.get(selectedTab);

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

                    Component page = tabsToPages.get(selectedTab);
                    tabs.remove(selectedTab);
                    pages.remove(page);
                    tabsToPages.remove(page);
                });
                selectedTab.add(close);

                currentNewTab = new Tab(new Icon(VaadinIcon.PLUS));
                VerticalLayout newPage = new VerticalLayout();
                tabs.add(currentNewTab);
                newPage.setVisible(false);
                tabsToPages.put(currentNewTab, newPage);
                pages.add(newPage);

                convertingPage.add(new Text("Page" + tabCounter++));
            }

            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
        });
        add(tabs, pages);
    }
}
