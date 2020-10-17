package org.aksw.facete3.app.vaadin.providers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromCollection;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromCollectionImpl;
import org.aksw.facete3.app.vaadin.ConfigNli.NliConfig;
import org.aksw.facete3.app.vaadin.domain.NliResponse;
import org.aksw.facete3.app.vaadin.domain.Paper;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchProviderNli
    implements SearchProvider
{
    public static final Property score = ResourceFactory.createProperty("http://nli.aksw.org/score");

    protected NliConfig config;

    public SearchProviderNli(NliConfig config) {
        super();
        Objects.requireNonNull(config);
        this.config = config;
    }

    public NliConfig getConfig() {
        return config;
    }

    @Override
    public RDFNodeSpecFromCollection search(String query) {
        URI uri = UriComponentsBuilder.fromUriString(config.getEnpoint())
                .queryParam("query", query)
                .queryParam("limit", config.getResultLimit())
                .build()
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        NliResponse response = restTemplate.getForObject(uri, NliResponse.class);


        List<RDFNode> resultList = new ArrayList<>();

        for (Paper paper : response.getResults()) {
            for (String iri : paper.getIds()) {
                Double scoreValue = paper.getScore();

                Resource r = ModelFactory.createDefaultModel().createResource(iri);
                ResourceUtils.setLiteralProperty(r, score, scoreValue);

                resultList.add(r);
            }
        }

        RDFNodeSpecFromCollection result = new RDFNodeSpecFromCollectionImpl(resultList);

        return result;
    }

}
