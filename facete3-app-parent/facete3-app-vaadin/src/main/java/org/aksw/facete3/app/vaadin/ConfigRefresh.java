package org.aksw.facete3.app.vaadin;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

// @Import(RefreshAutoConfiguration.class)
public class ConfigRefresh {

    @Bean
    public static RefreshScope refreshScope() {
        return new RefreshScope();
    }

    @Bean
    public ContextRefresher contextRefresher(ConfigurableApplicationContext context, RefreshScope scope) {
        return new ContextRefresher(context, scope);
    }

    @Bean
    public CustomScopeConfigurer servletCustomScopeConfigurer(
            RefreshScope refreshScope) {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
        customScopeConfigurer.addScope("refresh", refreshScope);
        return customScopeConfigurer;
    }
}
