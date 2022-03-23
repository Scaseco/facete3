package org.aksw.facete3.cli.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.aksw.commons.picocli.VersionProviderFromClasspathProperties;

public class VersionProviderFacete3
    extends VersionProviderFromClasspathProperties
{
    @Override public String getResourceName() { return "facete3.metadata.properties"; }
    @Override public Collection<String> getStrings(Properties p) {
        return Arrays.asList(p.get("facete3.version") + " built at " + p.get("facete3.build.timestamp"));
    }
}
