package org.aksw.facete3.app.vaadin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("facete3.nli")
public class NliConfig {
    private String endpoint;
    private Long resultLimit;

    public String getEndpoint() {
        return endpoint;
    }

    public Long getResultLimit() {
        return resultLimit;
    }

    public void setResultLimit(Long resultLimit) {
        this.resultLimit = resultLimit;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}