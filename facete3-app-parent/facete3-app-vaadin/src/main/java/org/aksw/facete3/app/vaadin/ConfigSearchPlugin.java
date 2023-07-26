package org.aksw.facete3.app.vaadin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.aksw.facete3.app.vaadin.plugin.ComponentPlugin;
import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.aksw.facete3.app.vaadin.qualifier.SearchPluginProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

public class ConfigSearchPlugin {

    @Autowired
    protected ConfigurableApplicationContext cxt;

    @Bean
    @SearchPluginProvider
    public InMemoryDataProvider<SearchPlugin> searchPluginProvider() {
        // List<Class<?>> cxts = Arrays.asList(ConfigSearchProviderNli.class, ConfigSearchProviderSparql.class);
        List<Class<?>> cxts = Arrays.asList(ConfigSearchProviderSparql.class);

        Collection<SearchPlugin> searchPlugins = new ArrayList<>();
        for (Class<?> pluginCxt : cxts) {
            SearchPlugin plugin = ComponentPlugin.defaultBaseAppBuilder()
                .parent(cxt)
                .sources(pluginCxt)
                .run()
                .getBean(SearchPlugin.class);

            searchPlugins.add(plugin);
        }

        ListDataProvider<SearchPlugin> result = new ListDataProvider<>(searchPlugins);
        return result;
    }
}
