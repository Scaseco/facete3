package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.List;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.connection.extra.RDFConnectionMetaData;
import org.apache.jena.rdf.model.Resource;

public interface SparqlDistribution
    extends Resource
{
    @Iri("eg:serviceURL")
    String getServiceUrl();

    @IriNs("eg")
    List<String> getNamedGraphs();

    @IriNs("eg")
    List<String> getDefaultGraphs();

    RDFConnectionMetaData setServiceUrl(String url);


    /**
     * Yield information about which datasets this connection is associated with
     * Multiple items are seen as alternative identifiers for the datasets having ideally *exactly*
     * the same set of triples.
     * I.e. multiple items are not to be confused with a union of their data.
     *
     * TODO The items *should* be of type DcatDataset
     * TODO Handle the case where the information available through the connection only 'roughly' corresponds to a dataset
     *
     * @return
     */
    @Iri("eg:dataset")
    Set<Resource> getDatasets();
}
