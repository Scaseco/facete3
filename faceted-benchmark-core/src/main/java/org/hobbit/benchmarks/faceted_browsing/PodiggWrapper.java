package org.hobbit.benchmarks.faceted_browsing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodiggWrapper {

    private static final Logger logger = LoggerFactory.getLogger(PodiggWrapper.class);

    // GTFS_GEN_SEED=111 -e GTFS_GEN_REGION__SIZE_X=2000  -e GTFS_GEN_REGION__SIZE_Y=2000 -e GTFS_GEN_REGION__CELLS_PER_LATLON=200 -e GTFS_GEN_STOPS__STOPS=3500 -e GTFS_GEN_CONNECTIONS__DELAY_CHANCE=0.02 -e GTFS_GEN_CONNECTIONS__CONNECTIONS=4000 -e GTFS_GEN_ROUTES__ROUTES=3500 -e GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH=50 -e GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH=10  -e GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER=1.3  -e GTFS_GEN_CONNECTIONS__TIME_FINAL=31536000000

    public static void exec(String basePath, Path outputFolder, Map<String, String> env) throws IOException, InterruptedException {
        String cmd = basePath + "/bin/generate-env";

        ProcessBuilder processBuilder = new ProcessBuilder(cmd, outputFolder.toAbsolutePath().toString());
        Map<String, String> penv = processBuilder.environment();
        penv.putAll(env);

        SimpleProcessExecutor.wrap(processBuilder)
            .setOutputSink(System.out::println) //logger::debug)
            .execute();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("GTFS_GEN_SEED", "123");

        Path outputFolder = Paths.get("/tmp/podigg");
        //FileSystemUtils.deleteRecursively(outputFolder.toFile());

        exec("/home/raven/Projects/Eclipse/podigg-lc-bin", outputFolder, params);

        Path datasetFile = outputFolder.resolve("lc.ttl");

        Stream<Triple> triples = GraphUtils.createTripleStream(datasetFile.toString());
        System.out.println("Triples: " + triples.count());
    }
}
