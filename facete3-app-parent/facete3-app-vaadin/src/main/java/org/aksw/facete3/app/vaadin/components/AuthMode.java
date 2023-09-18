package org.aksw.facete3.app.vaadin.components;

public enum AuthMode {
    NONE("None"),
    BASIC("Basic"),
    BEARER_TOKEN("Bearer Token");

    private String name;

    AuthMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}