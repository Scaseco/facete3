package test;

import org.apache.jena.rdf.model.Model;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.evaluation.ChokePoints;
import org.hobbit.evaluation.InstancesEvalHelper;
import org.hobbit.evaluation.QueryID;
import org.hobbit.evaluation.ReturnModelBuilder;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by gkatsimpras on 9/12/2016.
 */
public class EvaluationModuleTest extends AbstractEvaluationModule {


    private int number_of_queries;
    private InstancesEvalHelperTest evalOverall;
    private HashMap<QueryIDTest, InstancesEvalHelperTest> evalCPTs;
    private ArrayList<QueryIDTest> queriesWithTimeout;

    private int tp;
    private int fn;
    private int fp;

    private int number_of_counts;
    private int count_error;
    private double count_error_ratio;
    private long sum_of_correct_count_results;
    private int count_time_needed;

    private int timeOut ;


    @Override
    public void init() throws Exception {

        evalOverall = new InstancesEvalHelperTest(0,0,0,0);
        evalCPTs = new HashMap<>();

        number_of_queries = 0 ;

        number_of_counts = 0;
        count_error = 0;
        count_error_ratio =0;
        sum_of_correct_count_results =0L;
        count_time_needed = 0;
        queriesWithTimeout = new ArrayList<>();
        timeOut = 1000000 ; // max time to answer a query in ms
    }

    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
                                             long responseReceivedTimestamp) throws Exception {


        ByteBuffer bufferRec = ByteBuffer.wrap(receivedData);
        String resultsString = RabbitMQUtils.readString(bufferRec);

        ByteBuffer bufferExp = ByteBuffer.wrap(expectedData);
        byte scenario = bufferExp.get();
        byte query = bufferExp.get();

        String goldsString = RabbitMQUtils.readString(bufferExp);

        String[] resultsArray = resultsString.split(",");
        for (int i=0 ; i< resultsArray.length; i++){
            resultsArray[i]=resultsArray[i].trim();
        }

        String[] goldsArray = goldsString.split(",");
        for (int i=0 ; i< goldsArray.length; i++){
            goldsArray[i]=goldsArray[i].trim();
        }

        ArrayList<String> receivedDataInstances = new ArrayList<>(Arrays.asList(resultsArray));
        ArrayList<String> expectedDataInstances = new ArrayList<>(Arrays.asList(goldsArray));

        QueryIDTest key = new QueryIDTest(scenario,query);

        if (scenario!=0) {

            number_of_queries +=1;

            tp = 0;
            fn = 0;

            // Evaluate the given response pair.

            for (String correct_instance : expectedDataInstances) {
                if (receivedDataInstances.contains(correct_instance)) {
                    tp += 1;
                } else {
                    fn += 1;
                }
            }
            fp = receivedDataInstances.size() - tp;

            int time_needed = responseReceivedTimestamp > 0L && responseReceivedTimestamp - taskSentTimestamp < timeOut ?
                    (int) (responseReceivedTimestamp - taskSentTimestamp) : timeOut;

            if(responseReceivedTimestamp == 0L){
                queriesWithTimeout.add(new QueryIDTest(scenario,query));
            }

            evalCPTs.put(key, new InstancesEvalHelperTest(tp, fn, fp, time_needed));

            evalOverall.add(tp, fn, fp, time_needed);


        }

        else {

            Integer receivedCount;
            try {
                receivedCount = Integer.parseInt(receivedDataInstances.get(0));
            } catch (NumberFormatException e) {
                receivedCount = 0;
            }

            Integer expectedCount = Integer.parseInt(expectedDataInstances.get(0));

            number_of_counts += 1;

            count_time_needed += responseReceivedTimestamp > 0L && responseReceivedTimestamp - taskSentTimestamp < timeOut?
                    (int) (responseReceivedTimestamp - taskSentTimestamp) : timeOut;

            if(responseReceivedTimestamp == 0L){
                queriesWithTimeout.add(new QueryIDTest(scenario,query));
            }


            int current_error = Math.abs(receivedCount - expectedCount);

            count_error += current_error;
            count_error_ratio += (double) current_error / expectedCount;
            sum_of_correct_count_results += expectedCount;

        }
    }




    @Override
    protected Model summarizeEvaluation ()throws Exception {


        // ______________________________________
        // 1.a) Overall results
        // ______________________________________

        double overall_query_per_second_score;
        double overall_precision;
        double overall_recall;
        double overall_f1;
        // ______________________________________
        // 1.a.i) Time score in queries per second
        // ______________________________________

        overall_query_per_second_score = (double) number_of_queries / evalOverall.getTime();

        // ______________________________________
        // 1.a.ii) Precision, recall, F1-measure
        // ______________________________________


        // Compute precision, recall and f1-measure

        overall_precision = (double) evalOverall.getTP() / (evalOverall.getTP() + evalOverall.getFP());
        overall_recall = (double) evalOverall.getTP() / (evalOverall.getTP() + evalOverall.getFN());
        overall_f1 = 2 * overall_precision * overall_recall / (overall_precision + overall_recall);

        // ______________________________________
        // 1.b) Results per choke point
        // ______________________________________

        HashMap<Integer, Double> chokePT_query_per_second_score = new HashMap<>();
        HashMap<Integer, Double> chokePT_precision = new HashMap<>();
        HashMap<Integer, Double> chokePT_recall = new HashMap<>();
        HashMap<Integer, Double> chokePT_f1 = new HashMap<>();

        HashMap<Integer, ArrayList<QueryIDTest>> chokePointsTable = ChokePointsTest.getTable();

        // ______________________________________
        // 1.b.i) Time score in queries per second
        // ______________________________________

        // For each choke point, find the queries related to it with help of the choke points table
        // and sum the time needed over all those queries
        // Then for each choke point return the query-per-seconds score as in 1.a.i

        // loop over choke points
        Set<Map.Entry<Integer, ArrayList<QueryIDTest>>> chokePT_entries = chokePointsTable.entrySet();

        for (Map.Entry<Integer, ArrayList<QueryIDTest>> chokePt_entry : chokePT_entries) {
            Integer key = chokePt_entry.getKey();
            List<QueryIDTest> queries = chokePt_entry.getValue();

            int current_chokePT_number_of_queries = 0;
            InstancesEvalHelperTest currentChokePT_eval = new InstancesEvalHelperTest(0,0,0,0);

            for (QueryIDTest query : queries) {
                current_chokePT_number_of_queries += 1;
                currentChokePT_eval.add(evalCPTs.get(query));
            }


            double current_chokePT_query_per_second_score = (double) current_chokePT_number_of_queries / currentChokePT_eval.getTime();
            double current_chokePT_precision = (double) currentChokePT_eval.getTP() / (currentChokePT_eval.getTP() + currentChokePT_eval.getFP());
            double current_chokePT_recall = (double) currentChokePT_eval.getTP() / (currentChokePT_eval.getTP() + currentChokePT_eval.getFN());
            double current_chokePT_f1 = 2 * current_chokePT_precision * current_chokePT_recall / (current_chokePT_precision + current_chokePT_recall);



            chokePT_query_per_second_score.put(key, current_chokePT_query_per_second_score);
            chokePT_precision.put(key, current_chokePT_precision);
            chokePT_recall.put(key, current_chokePT_recall);
            chokePT_f1.put(key, current_chokePT_f1);


        }

        // ______________________________________
        // 2.) COUNT queries
        // ______________________________________


        // ______________________________________
        // 2.a.i) Time score in queries per second
        // ______________________________________


        double count_per_second_score = (double) number_of_counts / count_time_needed;


        // ______________________________________
        // 2.a.ii) Correctness of counts
        // ______________________________________

        long overall_error = count_error;
        double average_error = (double) count_error/number_of_counts;
        double overall_error_ratio =(double)  count_error/sum_of_correct_count_results;
        double average_error_ratio =  count_error_ratio/number_of_counts;

        // create a rdf model which will hold the results


        return ReturnModelBuilderTest.build(experimentUri,
                overall_query_per_second_score,
                overall_precision,
                overall_recall,
                overall_f1,
                chokePT_query_per_second_score,
                chokePT_precision,
                chokePT_recall,
                chokePT_f1,
                overall_error,
                average_error,
                overall_error_ratio,
                average_error_ratio,
                count_per_second_score,
                queriesWithTimeout);

    }
}

