package alpha_vs;

import java.util.*;

/**
 * Created by hpetzka on 09.12.2016.
 */
public class InstanceEvaluationModule {

    private HashMap<Pair<Integer,Integer>,Pair<ArrayList<String>,Double>> results;
    private HashMap<Pair<Integer,Integer>, ArrayList<String>> gold_standard_instances;
    private HashMap<Integer, ArrayList<Pair<Integer, Integer>>> choke_points_table;


    public InstanceEvaluationModule(HashMap<Pair<Integer,Integer>,Pair<ArrayList<String>,Double>> results,
                                    HashMap<Pair<Integer,Integer>, ArrayList<String>> gold_standard_instances,
                                    HashMap<Integer, ArrayList<Pair<Integer, Integer>>> choke_points_table) {

        this.results = results;
        this.gold_standard_instances = gold_standard_instances;
        this.choke_points_table = choke_points_table;
    }


    public EvaluationResult evaluate(){


        double overall_time_needed = 0.0 ;
        int number_of_queries = 0 ;
        int overall_tp = 0;
        int overall_fn =0;
        int overall_fp = 0;


        double overall_query_per_second_score;
        double overall_precision;
        double overall_recall;
        double overall_f1;

        // As a preparation compute the true positive, false negatives, total positives and false positives

        HashMap<Pair<Integer,Integer>, Integer > true_pos = new HashMap<>();
        HashMap<Pair<Integer,Integer>, Integer > false_neg = new HashMap<>();
        HashMap<Pair<Integer,Integer>, Integer > false_pos = new HashMap<>();

        Set<Map.Entry<Pair<Integer,Integer>,Pair<ArrayList<String>, Double> >> entries = this.results.entrySet();
        for (Map.Entry<Pair<Integer,Integer>,Pair<ArrayList<String>, Double> > entry : entries) {

            Pair<Integer,Integer> key = entry.getKey();
            Pair<ArrayList<String>, Double> result = entry.getValue();

            List<String> result_instances = result.getFirst();

            int tp=0;
            int fn=0;
            int fp;

            for (String correct_instance : this.gold_standard_instances.get(key)){
                if(result_instances.contains(correct_instance)){
                    tp+=1;
                } else {
                    fn +=1;
                }
            }
            fp = result_instances.size() - tp;

            true_pos.put(key , tp );
            false_neg.put(key , fn );
            false_pos.put(key , fp );


            overall_time_needed += result.getSecond();
            number_of_queries += 1;
            overall_tp += tp ;
            overall_fp += fp;
            overall_fn += fn ;

        }

        // ______________________________________
        // 1.a) Overall results
        // ______________________________________

        // ______________________________________
        // 1.a.i) Time score in queries per second
        // ______________________________________

        overall_query_per_second_score =  number_of_queries/overall_time_needed;

        // ______________________________________
        // 1.a.ii) Precision, recall, F1-measure
        // ______________________________________


        // Compute precision, recall and f1-measure

        overall_precision = (double) overall_tp / (overall_tp + overall_fp);
        overall_recall = (double) overall_tp / (overall_tp + overall_fn);
        overall_f1 = 2 * overall_precision * overall_recall / (overall_precision + overall_recall);


        // ______________________________________
        // 1.b) Results per choke point
        // ______________________________________

        // ______________________________________
        // 1.b.i) Time score in queries per second
        // ______________________________________

        // For each choke point, find the queries related to it with help of the choke points table
        // and sum the time needed over all those queries
        // Then for each choke point return the query-per-seconds score as in 1.a.i


        //TODO maybe it is quicker to add the values in loop above for each choke point acoording to table
        //TODO and to use a table that works the other way round

        HashMap<Integer, Double> chokePT_query_per_second_score = new HashMap<>();
        HashMap<Integer, Double> chokePT_precision = new HashMap<>();
        HashMap<Integer, Double> chokePT_recall = new HashMap<>();
        HashMap<Integer, Double> chokePT_f1 = new HashMap<>();

        // loop over choke points
        Set <Map.Entry<Integer,ArrayList<Pair<Integer,Integer> >>> chokePT_entries = this.choke_points_table.entrySet();

        for(Map.Entry<Integer ,ArrayList<Pair<Integer, Integer>>> chokePt_entry : chokePT_entries) {
            Integer key = chokePt_entry.getKey();
            List<Pair<Integer,Integer>> result = chokePt_entry.getValue();

            double current_chokePT_number_of_queries = 0;
            double current_chokePT_time = 0;
            double current_chokePT_tp = 0;
            double current_chokePT_fn = 0;
            double current_chokePT_fp = 0;




            for (Pair<Integer,Integer> pair : result){
                current_chokePT_number_of_queries +=1;
                current_chokePT_time += this.results.get(pair).getSecond();
                current_chokePT_tp += true_pos.get(pair);
                current_chokePT_fn += false_neg.get(pair);
                current_chokePT_fp += false_pos.get(pair);
            }


            double current_chokePT_query_per_second_score = current_chokePT_number_of_queries/current_chokePT_time;
            double current_chokePT_precision = current_chokePT_tp / (current_chokePT_tp + current_chokePT_fp);
            double current_chokePT_recall = current_chokePT_tp / (current_chokePT_tp + current_chokePT_fn);
            double current_chokePT_f1 = 2 * current_chokePT_precision * current_chokePT_recall / (current_chokePT_precision + current_chokePT_recall);

            chokePT_query_per_second_score.put(key, current_chokePT_query_per_second_score);
            chokePT_precision.put(key, current_chokePT_precision);
            chokePT_recall.put(key, current_chokePT_recall);
            chokePT_f1.put(key, current_chokePT_f1);

        }

        // ______________________________________
        // 1.c.ii) Precision, recall, F1-measure per Scenario ???
        // ______________________________________

        // Maybe we want to sum over each scenario, too
        // This shouldn't be much more work, but might enable better comparisons of competing systems

        // ______________________________________


        // ______________________________________
        // 2.) COUNT queries
        // ______________________________________



        return new EvaluationResult(

                overall_query_per_second_score,
                overall_precision,
                overall_recall,
                overall_f1,

                chokePT_query_per_second_score,
                chokePT_precision,
                chokePT_recall,
                chokePT_f1
                );


    }


}
