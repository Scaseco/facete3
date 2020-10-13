package org.aksw.facete3.cli.main;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Facete3 Options")
public class CmdFacete3Main {
    @Parameter(description="Sources, one or more file names or a single sparql endpoint URL")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Parameter(names= {"-b", "--bp", "--bnode-profile"}, description="Blank node profile, empty string ('') to disable")
    public String bnodeProfile = "auto";

    @Parameter(names="-c", description="Base concept, e.g. SELECT ?s {?s a foaf:Person}")
    public String baseConcept = "";

    @Parameter(names="-p", description="Prefix Sources")
    public List<String> prefixSources = new ArrayList<>();

    @Parameter(names="-u", description="Union default graph mode (for quad-based formats)")
    public boolean unionDefaultGraphMode = false;

    @Parameter(names="-g", description="Default graphs (applies to quad based data sources)")
    public List<String> defaultGraphs = new ArrayList<>();

    @Parameter(names={"-h", "--help"}, help = true)
    public boolean help = false;
}
