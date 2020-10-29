package org.aksw.facete3.app.vaadin.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

public class PreconfiguredTabs
    extends Div
{
    private static final long serialVersionUID = 1L;

    protected Tabs tabs = new Tabs();
    protected Div pages = new Div();

    protected Map<Tab, ManagedComponent> tabsToPages = new LinkedHashMap<>();

    // TODO The supplier is not a view-model; there should be some
    // DataProvider that provides the components
    public PreconfiguredTabs() {
        this.setWidthFull();
        this.setHeightFull();
        pages.setWidthFull();
        pages.setHeightFull();

        tabs.addSelectedChangeListener(ev -> {
            tabsToPages.values().forEach(page -> page.getComponent().setVisible(false));

            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab != null) {
                Component selectedPage = tabsToPages.get(selectedTab).getComponent();
                selectedPage.setVisible(true);
            }
        });
        add(tabs, pages);
    }


    /**
     * Append a new tab instance to the tabs component
     * This method is also invoked when the 'new tab' button is clicked
     *
     */
    public void newTab(String name, ManagedComponent content) {
        System.out.println("ADDING TAB " + name);

        Tab newTab = new Tab(new Text(name)); //new Tab(new Icon(VaadinIcon.PLUS));
        newTab.setClassName("compact");

        Component contentComponent = content.getComponent();

        tabsToPages.put(newTab, content);
        tabs.add(newTab);
        pages.add(contentComponent);

        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) {
            tabs.setSelectedTab(newTab);
        }
    }

    protected void destroyTab(Tab tab) {


        ManagedComponent page = tabsToPages.get(tab);
        pages.remove(page.getComponent());
        tabs.remove(tab);
        tabsToPages.remove(tab);

        page.close();
    }

    void removeAllTabs() {
        tabs.setSelectedTab(null);

        List<Tab> tabList = new ArrayList<>(tabsToPages.keySet());
        Collections.reverse(tabList);

        for (Tab tab : tabList) {
            destroyTab(tab);
        }
    }
}
