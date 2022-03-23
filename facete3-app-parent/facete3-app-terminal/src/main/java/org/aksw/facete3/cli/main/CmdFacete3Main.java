package org.aksw.facete3.cli.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.commons.picocli.HasDebugMode;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(separator = "=", description = "Facete3 Options", mixinStandardHelpOptions = true, versionProvider = VersionProviderFacete3.class)
public class CmdFacete3Main
    implements HasDebugMode, Callable<Integer>
{
    @Parameters(description="Sources, one or more file names or a single sparql endpoint URL")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Option(names= {"-b", "--bp", "--bnode-profile"}, description="Blank node profile, empty string ('') to disable")
    public String bnodeProfile = "auto";

    @Option(names="-c", description="Base concept, e.g. SELECT ?s {?s a foaf:Person}")
    public String baseConcept = "";

    @Option(names="-p", description="Prefix Sources")
    public List<String> prefixSources = new ArrayList<>();

    @Option(names="-u", description="Union default graph mode (for quad-based formats)")
    public boolean unionDefaultGraphMode = false;

    @Option(names="-g", description="Default graphs (applies to quad based data sources)")
    public List<String> defaultGraphs = new ArrayList<>();

    @Option(names = { "-X" }, description = "Debug output such as full stacktraces")
    public boolean debugMode = false;

    @Override
    public boolean isDebugMode() {
        return debugMode;
    }

    @Override
    public Integer call() throws Exception {
        MainCliFacete3.run(this);
        return 0;
    }
}
