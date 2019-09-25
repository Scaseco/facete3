package org.hobbit.core.component;

import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.vocab.HOBBIT;
import org.hobbit.vocab.HobbitErrors;

public class BenchmarkControllerUtils {
//	
//    // Get the experiment URI
//    if (env.containsKey(Constants.HOBBIT_EXPERIMENT_URI_KEY)) {
//        experimentUri = env.get(Constants.HOBBIT_EXPERIMENT_URI_KEY);
//    } else {
//        String errorMsg = "Couldn't get the experiment URI from the variable " + Constants.HOBBIT_EXPERIMENT_URI_KEY
//                + ". Aborting.";
//        LOGGER.error(errorMsg);
//        throw new Exception(errorMsg);
//    }
//
//    /**
//     * Sends the result RDF model to the platform controller.
//     *
//     * @param model
//     *            model containing the results
//     */
//    protected void sendResultModel(Model model) {
//        try {
//            resultModelMutex.acquire();
//        } catch (InterruptedException e) {
//            LOGGER.error("Interrupted while waiting for the result model mutex. Returning.", e);
//        }
//        try {
//            if (systemExitCode != 0) {
//                model.add(model.getResource(experimentUri), HOBBIT.terminatedWithError, HobbitErrors.SystemCrashed);
//            }
//            sendToCmdQueue(Commands.BENCHMARK_FINISHED_SIGNAL, RabbitMQUtils.writeModel(model));
//        } catch (IOException e) {
//            String errorMsg = "Exception while trying to send the result to the platform controller.";
//            LOGGER.error(errorMsg);
//            throw new IllegalStateException(errorMsg, e);
//        } finally {
//            resultModelMutex.release();
//        }
//    }
    /**
     * Adds the {@link #benchmarkParamModel} triples to the {@link #resultModel}
     * .
     */
//    protected void addParametersToResultModel() {
//        try {
//            resultModelMutex.acquire();
//        } catch (InterruptedException e) {
//            LOGGER.error("Interrupted while waiting for the result model mutex. Returning.", e);
//        }
//        try {
//            Resource experimentResource = resultModel.getResource(experimentUri);
//            StmtIterator iterator = benchmarkParamModel.listStatements(
//                    benchmarkParamModel.getResource(Constants.NEW_EXPERIMENT_URI), null, (RDFNode) null);
//            Statement statement;
//            while (iterator.hasNext()) {
//                statement = iterator.next();
//                resultModel.add(experimentResource, statement.getPredicate(), statement.getObject());
//            }
//        } finally {
//            resultModelMutex.release();
//        }
//    }
//    
    /**
     * Generates a default model containing an error code and the benchmark
     * parameters if no result model has been received from the evaluation
     * module until now. If the model already has been received, the error is
     * added to the existing model.
     */
//    protected void generateErrorResultModel() {
//        try {
//            resultModelMutex.acquire();
//        } catch (InterruptedException e) {
//            LOGGER.error("Interrupted while waiting for the result model mutex. Returning.", e);
//        }
//        try {
//            if (resultModel == null) {
//                this.resultModel = ModelFactory.createDefaultModel();
//                resultModel.add(resultModel.getResource(experimentUri), RDF.type, HOBBIT.Experiment);
//            }
//            resultModel.add(resultModel.getResource(experimentUri), HOBBIT.terminatedWithError,
//                    HobbitErrors.BenchmarkCrashed);
//        } finally {
//            resultModelMutex.release();
//        }
//        addParametersToResultModel();
//    }
//
////    /**
////     * Uses the given model as result model if the result model is
////     *dataGenerator <code>null</code>. Else, the two models are merged.
//     *
//     * @param resultModel
//     *            the new result model
//     */
//    protected void setResultModel(Model resultModel) {
//        try {
//            resultModelMutex.acquire();
//        } catch (InterruptedException e) {
//            LOGGER.error("Interrupted while waiting for the result model mutex. Returning.", e);
//        }
//        try {
//            if (this.resultModel == null) {
//                this.resultModel = resultModel;
//            } else {
//                this.resultModel.add(resultModel);
//            }
//        } finally {
//            resultModelMutex.release();
//        }
//        addParametersToResultModel();
//    }
    
//    protected void sendResultModel(Model model) {
//        try {
//            resultModelMutex.acquire();
//        } catch (InterruptedException e) {
//            LOGGER.error("Interrupted while waiting for the result model mutex. Returning.", e);
//        }
//        try {
//            if (systemExitCode != 0) {
//                model.add(model.getResource(experimentUri), HOBBIT.terminatedWithError, HobbitErrors.SystemCrashed);
//            }
//            sendToCmdQueue(Commands.BENCHMARK_FINISHED_SIGNAL, RabbitMQUtils.writeModel(model));
//        } catch (IOException e) {
//            String errorMsg = "Exception while trying to send the result to the platform controller.";
//            LOGGER.error(errorMsg);
//            throw new IllegalStateException(errorMsg, e);
//        } finally {
//            resultModelMutex.release();
//        }
//    }

}
