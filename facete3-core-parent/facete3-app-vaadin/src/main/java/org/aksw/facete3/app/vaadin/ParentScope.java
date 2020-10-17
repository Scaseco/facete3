package org.aksw.facete3.app.vaadin;

// Based on https://github.com/spring-cloud/spring-cloud-config/issues/1116
public class ParentScope {

//    @Bean
//    public CustomScopeConfigurer servletCustomScopeConfigurer(org.springframework.cloud.context.scope.refresh.RefreshScope refreshScope) {
//        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer();
//        customScopeConfigurer.addScope("refresh", refreshScope);
//        return customScopeConfigurer;
//    }
//
//    @Bean
//    public DispatcherServlet dispatcherServlet(ConfigurableEnvironment parentEnvironment) {
//        DispatcherServlet dispatcherServlet = new DispatcherServlet();
//        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
//        //applicationContext.register(WebConfiguration.class);
//        applicationContext.setEnvironment(parentEnvironment);
//        dispatcherServlet.setApplicationContext(applicationContext);
//        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
//        return dispatcherServlet;
//    }
}
