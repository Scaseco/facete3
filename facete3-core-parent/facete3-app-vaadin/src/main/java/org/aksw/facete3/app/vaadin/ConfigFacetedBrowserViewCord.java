package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.config.ConfigViewManager;
import org.springframework.context.annotation.Import;

@Import({
    ConfigEndpoint.class,
    ConfigSearchProviderNli.class,
    ConfigViewManager.class,
    ConfigFacetedBrowserView.class})
public class ConfigFacetedBrowserViewCord {

}
