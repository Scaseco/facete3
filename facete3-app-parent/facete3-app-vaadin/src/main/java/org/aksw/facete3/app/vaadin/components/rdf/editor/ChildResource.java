package org.aksw.facete3.app.vaadin.components.rdf.editor;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

/**
 * Resource view for expressing how to
 * compute an IRI for the viewed resource.
 *
 * There iriValue attribute is interpreted in the context of a parent (may be nul) and an IRI mode.
 *
 * <ul>
 *   <li>Absolute: Interpret iriValue as an absolute IRI</li>
 *   <li>Relative: Append iriValue to the parent's (effective) IRI. The parent's effective IRI may need to be computed first.</li>
 * <ul>
 *
 *
 * @author raven
 *
 */
@ResourceView
public interface ChildResource
    extends Resource
{
    @IriNs("eg")
    Resource getParent();
    ChildResource setParent(Resource parent);

    @IriNs("eg")
    String getIriValue();
    ChildResource setIriValue(String iriValue);

    @IriNs("eg")
    String getIriMode();
    ChildResource setIriMode(String iriValue);


//    default ChildResource setIriModeRelative() {
//
//    }
}
