package org.hobbit.benchmarks.faceted_browsing.components;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import javax.annotation.Resource;

import org.hobbit.core.Commands;
import org.hobbit.interfaces.BenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkControllerFacetedBrowsing
    implements BenchmarkController
{
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkControllerFacetedBrowsing.class);


    @Resource(name="cmdQueue")
    protected WritableByteChannel toCmdQueue;

    @Override
    public void init() throws Exception {
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        logger.info("Seen command: " + command + " with " + data.length + " bytes");
    }

    @Override
    public void executeBenchmark() throws Exception {

        logger.info("Benchmark execution initiated");


        toCmdQueue.write(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));
        toCmdQueue.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}));


//
//        // wait for the data generators to finish their work
//        LOGGER.info("WAITING FOR DATA GENERATOR ...");
//        waitForDataGenToFinish();
//        // wait for the task generators to finish their work
//        // wait for the system to terminate
//        waitForTaskGenToFinish();
//        LOGGER.info("WAITING FOR SYSTEM ...");
//        waitForSystemToFinish();
//        this.stopContainer(containerName);
//
//
//        // Create the evaluation module
//        String evalModuleImageName = "git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image";
//        String[] envVariables = new String[]{"NO_VAR=true"};
//        createEvaluationModule(evalModuleImageName, envVariables);
//        // wait for the evaluation to finish
//        waitForEvalComponentsToFinish();
//        // the evaluation module should have sent an RDF model containing the
//        // results. We should add the configuration of the benchmark to this
//        // model.
//        // this.resultModel.add(...);
//        // Send the resultModul to the platform controller and terminate
//        sendResultModel(resultModel);
    }
}



