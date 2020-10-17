package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ServiceStatus
    extends Resource
{
    @IriNs("http://www.w3.org/ns/sparql-service-description#")
    @IriType
    String getEndpoint();
    ServiceStatus setEndpoint(String url);

    @IriNs("https://schema.org/")
    @IriType
    String getServerStatus();
    ServiceStatus setServerStatus(String serverStatus);

    @IriNs("https://schema.org/")
    XSDDateTime getDateModified();
    ServiceStatus setDateModified(XSDDateTime dateTime);

}
