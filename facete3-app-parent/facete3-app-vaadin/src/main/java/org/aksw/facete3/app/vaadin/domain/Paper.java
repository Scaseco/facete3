package org.aksw.facete3.app.vaadin.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Paper {

    private String corpus;
    private List<String> id = null;
    private String model;
    private Integer rank;
    private Double score;
    private String text;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getCorpus() {
        return corpus;
    }

    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }

    public List<String> getIds() {
        return id;
    }

    public void setId(List<String> id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
