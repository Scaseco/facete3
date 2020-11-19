package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.config.ConfigViewManager;
import org.springframework.context.annotation.Import;

@Import({
    ConfigBestLabel.class,
    ConfigEndpoint.class,
//    ConfigSearchProviderNli.class,
    ConfigSearchPlugin.class,
    ConfigViewManager.class,
    ConfigFacetedBrowserView.class})
public class ConfigFacetedBrowserViewCord {

}
