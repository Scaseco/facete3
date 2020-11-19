package org.aksw.facete3.app.vaadin.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.facete3.app.vaadin.plugin.ManagedComponent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

public class PreconfiguredTabs
    extends Div
{
    private static final long serialVersionUID = 1L;

    protected Tabs tabs = new Tabs();
    protected Div pages = new Div();

    protected BiMap<String, Tab> idToTab = HashBiMap.create();
    protected Map<Tab, ManagedComponent> tabsToPages = new LinkedHashMap<>();

    public PreconfiguredTabs() {
        this(new VerticalLayout());
    }

    // TODO The supplier is not a view-model; there should be some
    // DataProvider that provides the components
    public PreconfiguredTabs(Component container) {
        this.setWidthFull();
        this.setHeightFull();
        pages.setWidthFull();
        pages.setHeightFull();

//        HorizontalLayout container = new HorizontalLayout();

        tabs.addSelectedChangeListener(ev -> {
            Tab selectedTab = tabs.getSelectedTab();
            setSelectedTab(selectedTab);
        });

        ((HasSize)container).setWidthFull();
        ((HasSize)container).setHeightFull();
        ((HasComponents)container).add(tabs, pages);
        add(container);
    }


    /**
     * Append a new tab instance to the tabs component
     * This method is also invoked when the 'new tab' button is clicked
     *
     */
    public void newTab(String id, String name, ManagedComponent content) {
        System.out.println("ADDING TAB " + name);

        Tab priorTab = idToTab.get(id);
        if (priorTab != null) {
            destroyTab(priorTab);
        }

        Tab newTab = new Tab(new Text(name)); //new Tab(new Icon(VaadinIcon.PLUS));
        newTab.setClassName("compact");


        Component contentComponent = content.getComponent();

        idToTab.put(id, newTab);
        tabsToPages.put(newTab, content);
        tabs.add(newTab);
        pages.add(contentComponent);

        Tab selectedTab = tabs.getSelectedTab();
        if (selectedTab == null) {
            tabs.setSelectedTab(newTab);
        }
    }

    public String getSelectedTabId() {
        Tab tab = tabs.getSelectedTab();
        String id = idToTab.inverse().get(tab);
        return id;
    }

    public void setSelectedTabId(String id) {
        Tab tab = idToTab.get(id);
        setSelectedTab(tab);
    }

    public void setSelectedTab(Tab selectedTab) {
        tabsToPages.values().forEach(page -> page.getComponent().setVisible(false));
        tabs.setSelectedTab(selectedTab);
        if (selectedTab != null) {
            Component selectedPage = tabsToPages.get(selectedTab).getComponent();
            selectedPage.setVisible(true);
        }
    }

    public Tabs getTabsComponent() {
        return tabs;

    }
    public Collection<Tab> getAvailableTabs() {
        return tabsToPages.keySet();
    }

    public void destroyTab(String id) {
        Tab tab = idToTab.get(id);
        if (tab != null) {
            destroyTab(tab);
        }
    }

    protected void destroyTab(Tab tab) {
        ManagedComponent page = tabsToPages.get(tab);
        pages.remove(page.getComponent());
        tabs.remove(tab);
        tabsToPages.remove(tab);
        idToTab.inverse().remove(tab);

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
