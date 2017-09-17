package org.hobbit.benchmarks.faceted_browsing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.connection.SparqlUpdateConnectionJsa;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

/**
 * A service wrapper instance manages a single underlying service process.
 *
 * TODO Factory all the system command + health checking stuff out into an AbstractSystemService
 *
 * @author raven
 *
 */
public class VirtuosoServiceWrapper
    extends AbstractIdleService
{
    private static final Logger logger = LoggerFactory.getLogger(VirtuosoServiceWrapper.class);

    protected ExecutorService executorService;

    protected Path virtExecPath;
    protected Path virtIniPath;

    // The working directory. By default, the folder of the ini file
    protected Path workingPath;

    protected Duration healthCheckInterval = Duration.ofSeconds(3);
    // protected int healthCheckRetries = 10;

    // The parsed ini file, used to read out ports
    protected transient Ini virtIni;
    protected transient Preferences virtIniPrefs;

    protected transient Process[] process = { null };

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public VirtuosoServiceWrapper setHealthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }

    // public int getHealthCheckRetries() {
    // return healthCheckRetries;
    // }
    //
    // public VirtuosoServiceWrapper setHealthCheckRetries(int
    // healthCheckRetries) {
    // this.healthCheckRetries = healthCheckRetries;
    // return this;
    // }

    // A single instance of the shutdown hook thread
    protected Thread shutdownHookThread = new Thread(() -> {
        System.err.println("Shutdown hook: terminating virtuoso process");
        if (process[0] != null) {
            process[0].destroy();
        }
    });

    public VirtuosoServiceWrapper(Path virtExecPath, Path virtIniPath) {
        super();
        this.virtExecPath = virtExecPath;
        this.virtIniPath = virtIniPath;

        this.workingPath = virtIniPath.getParent();
    }

    /**
     * The default connection is some application specific connection. At
     * minimum it should enable health check queries.
     *
     *
     *
     * @return
     * @throws InvalidFileFormatException
     * @throws IOException
     */
    public RDFConnection createDefaultConnection() {
        // Autoconfigure URLs based on the ini file
        long odbcPort = virtIniPrefs.node("Parameters").getLong("ServerPort", -1);
        long httpPort = virtIniPrefs.node("HTTPServer").getLong("ServerPort", -1);

        String endpointUrl = "http://localhost:" + httpPort + "/sparql";

        SparqlService httpSparqlService = FluentSparqlService.http(endpointUrl).create();

        RDFConnection result = new RDFConnectionModular(
                new SparqlQueryConnectionJsa(httpSparqlService.getQueryExecutionFactory()),
                new SparqlUpdateConnectionJsa(httpSparqlService.getUpdateExecutionFactory()), null);

        System.out.println(odbcPort + " --- " + httpPort);

        return result;
    }

    /**
     * Perform a health check on the service
     *
     * @return
     */
    public boolean performHealthCheck() {
        boolean result = false;
        try (RDFConnection conn = createDefaultConnection()) {

            // A very basic select query, which all SPARQL systems
            // should be capable to process quickly, as usually it does not
            // match anything
            conn.querySelect("SELECT * { <http://example.org/healthcheck> a ?t }", (qs) -> {
            });
            result = true;
        } catch (Exception e) {
            logger.debug("Health check failed with reason: ", e);
        }

        logger.debug("Health check response: " + result);

        return result;
    }

    /**
     * Starts the service and yields a future indicating whether it became
     * healthy within a certain time limit.
     *
     * The service may still become healthy at a later stage, however,
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void startUp() throws IOException, InterruptedException {
        // Attempt to read the ini file
        virtIni = new Ini(virtIniPath.toFile());
        virtIniPrefs = new IniPreferences(virtIni);

        Runtime.getRuntime().addShutdownHook(shutdownHookThread);

        ProcessBuilder pb = new ProcessBuilder(virtExecPath.toString(), "-c", virtIniPath.toString(), "-f");
        pb.directory(workingPath.toFile());
        process[0] = SimpleProcessExecutor.wrap(pb).setService(true).execute();

        // Some simple retry policy on the health check
        // TODO Use async retry library (or something similar) for
        // having a powerful api for crafting retry policies

        // Start another thread that determines when the service becomes healthy
        try {
            boolean r = false;
            // for(int i = 0; ; ++i) {
            while (process[0] != null && process[0].isAlive()) {
                long millis = healthCheckInterval.toMillis();
                Thread.sleep(millis);
                r = performHealthCheck();
                if (r) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
            // If startup gets interrupted, shut down the service
            // throw new RuntimeException(e);
        }
    }

    @Override
    protected void shutDown() {

        // Things may get better with Java 9
        process[0].destroy();

        try {
            while (process[0].isAlive()) {
                // Waiting for process to die
                long millis = healthCheckInterval.toMillis();
                Thread.sleep(millis);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        process[0] = null;

        Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
    }

//    @Override
//    protected void doStart() {
//        try {
//            startImpl();
//            notifyStarted();
//        } catch (Exception e) {
//            notifyFailed(e);
//        }
//    }
//
//    @Override
//    protected void doStop() {
//        try {
//            stopImpl();
//            notifyStopped();
//        } catch (Exception e) {
//            notifyFailed(e);
//        }
//    }

    public static void main(String[] args) throws Exception {
        VirtuosoServiceWrapper virtService = new VirtuosoServiceWrapper(
                Paths.get("/opt/virtuoso/vos/7.2.4.2/bin/virtuoso-t"),
                Paths.get("/opt/virtuoso/vos/7.2.4.2/databases/hobbit_1112_8891/virtuoso.ini"));

        // CompletableFuture<Boolean> startupFuture = virtWrap.start();

        virtService.startAsync();
        logger.info("Waiting for startup..");
        try {
            virtService.awaitRunning(1, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            logger.info("Failure", e);
            virtService.stopAsync();
            return;
        }
        logger.info("Startup complete");

        // Alternative callback style
        // startupFuture.thenAccept(status -> {
        // System.out.println("Service is healthy");
        // });

        virtService.createDefaultConnection();

        Thread.sleep(5000);
        virtService.stopAsync();
        System.err.println("Stopping...");
        virtService.awaitTerminated();
    }

}
