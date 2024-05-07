package org.aksw.facete3.app.vaadin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.facete3.app.vaadin.components.DataProviderConnectorImpl;
import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderConnector;
import org.aksw.vaadin.common.provider.util.TaskControlRegistry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("facete3")
public class ConfigFaceteVaadin {

    protected Property alternativeLabelProperty;

    protected String[] prefixSources;


    @Autowired
    public ConfigurableApplicationContext context;

    public void setAlternativeLabel (String alternativeLabel) {
        this.alternativeLabelProperty = ResourceFactory.createProperty(alternativeLabel);
    }

    public Property getAlternativeLabel() {
        return this.alternativeLabelProperty;
    }

    @Bean
    public PrefixMapping globalPrefixMapping() {
        PrefixMapping result = new PrefixMappingImpl();
        if (prefixSources != null) {
            for (String prefixSource : prefixSources) {
                Model model = RDFDataMgr.loadModel(prefixSource);
                result.setNsPrefixes(model);
            }
        }
        return result;
    }

    public void setPrefixSources(String[] prefixSources) {
        this.prefixSources = prefixSources;
    }

    public String[] getPrefixSources(){
        return prefixSources;
    }

    @Bean
    public TaskControlRegistryImpl taskControlRegistry() {
        return new TaskControlRegistryImpl();
    }

    /** Executor for async processing */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        ExecutorService result = Executors.newCachedThreadPool();
        return result;
    }

    /**
     * The dataProviderConnector. Registers requests to the data provider with the taskControlRegistry.
     * Enables showing pending requests in the UI.
     */
    @Bean
    public DataProviderConnector dataProviderConnector(TaskControlRegistry taskControlRegistry, ExecutorService executorService) {
        return new DataProviderConnectorImpl(taskControlRegistry, executorService);
    }
}
