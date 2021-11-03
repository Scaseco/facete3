package org.aksw.facete3.app.vaadin;

import java.util.Arrays;

import org.aksw.facete3.app.vaadin.plugin.view.Bibframe;
import org.aksw.facete3.app.vaadin.qualifier.DisplayLabelConfig;
import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.aksw.jenax.arq.aggregation.LiteralPreference;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.context.annotation.Bean;

public class ConfigBestLabel {
    @Bean
    @DisplayLabelConfig
    public BestLiteralConfig bestLabelConfig() {
        LiteralPreference preference = new LiteralPreference(
                Arrays.asList("en", "de", ""),
                Arrays.asList(RDFS.label.asNode(), Bibframe.title.asNode()),
                false
        );

        BestLiteralConfig result = new BestLiteralConfig(preference);
        return result;
    }
}
