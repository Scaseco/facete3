package alpha_vs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hpetzka on 09.12.2016.
 */

public class test {

    public static void main(String[] args){

        HashMap<Pair<Integer, Integer>, Pair<ArrayList<String>, Double>> results = new HashMap<>();
        HashMap<Pair<Integer, Integer>, ArrayList<String>> golds = new HashMap<>();

        ArrayList<String> list_11 = new ArrayList<>();
        list_11.add("<http:/connection1>");
        list_11.add("<http:/connection2>");
        list_11.add("<http:/connection3>");
        results.put(new Pair<>(1, 1), new Pair<>(list_11, 4.7));

        ArrayList<String> gold_11 = new ArrayList<>();
        gold_11.add("<http:/connection1>");
        gold_11.add("<http:/connection2>");
        gold_11.add("<http:/connection4>");
        golds.put(new Pair<>(1, 1), gold_11);



        ArrayList<String> list_12 = new ArrayList<>();
        list_12.add("<http:/connection4>");
        list_12.add("<http:/connection5>");
        list_12.add("<http:/connection5>");
        results.put(new Pair<>(1, 2), new Pair<>(list_12, 2.3));

        ArrayList<String> gold_12 = new ArrayList<>();
        gold_12.add("<http:/connection4>");
        gold_12.add("<http:/connection5>");
        golds.put(new Pair<>(1, 2), gold_12);


        ArrayList<String> list_21 = new ArrayList<>();
        list_21.add("<http:/connection6>");
        list_21.add("<http:/connection7>");
        results.put(new Pair<>(2, 1), new Pair<>(list_21, 1.3));

        ArrayList<String> gold_21 = new ArrayList<>();
        gold_21.add("<http:/connection6>");
        gold_21.add("<http:/connection7>");
        gold_21.add("<http:/connection8>");
        golds.put(new Pair<>(2, 1), gold_21);


        ArrayList<String> list_22 = new ArrayList<>();
        list_22.add("<http:/connection8>");
        list_22.add("<http:/connection9>");
        results.put(new Pair<>(2, 2), new Pair<>(list_22, 3.8));

        ArrayList<String> gold_22 = new ArrayList<>();
        gold_22.add("<http:/connection8>");
        gold_22.add("<http:/connection9>");
        golds.put(new Pair<>(2, 2), gold_22);


        HashMap<Integer, ArrayList<Pair<Integer, Integer>>> choke_points_table = new HashMap<>();

        ArrayList<Pair<Integer,Integer>> choke_list_1 =  new ArrayList<>();
        choke_list_1.add(new Pair<>(1,1));
        choke_points_table.put(1, choke_list_1);

        ArrayList<Pair<Integer,Integer>> choke_list_2 =  new ArrayList<>();
        choke_list_2.add(new Pair<>(1,1));
        choke_list_2.add(new Pair<>(1,2));
        choke_points_table.put(2, choke_list_2);

        ArrayList<Pair<Integer,Integer>> choke_list_3 =  new ArrayList<>();
        choke_list_3.add(new Pair<>(2,1));
        choke_list_3.add(new Pair<>(2,2));
        choke_points_table.put(3, choke_list_3);


        int count_1 = 1;
        long count_1_result = 10L;
        double count_1_time_needed = 4.7;

        int count_2 = 2;
        long count_2_result = 14L;
        double count_2_time_needed = 3.3;

        HashMap<Integer, Pair<Long,Double>> counts_to_evaluate = new HashMap<>();

        counts_to_evaluate.put(count_1, new Pair<>(count_1_result, count_1_time_needed));
        counts_to_evaluate.put(count_2, new Pair<>(count_2_result, count_2_time_needed));

        HashMap<Integer, Long> count_golds = new HashMap<>();
        count_golds.put(1,9L);
        count_golds.put(2,16L);

        InstanceEvaluationModule evaluation_module = new InstanceEvaluationModule(results,golds,choke_points_table);
        CountEvaluationModule count_evaluation_module = new CountEvaluationModule(counts_to_evaluate,count_golds);

        EvaluationResult evaluation_results = evaluation_module.evaluate();
        CountResult count_results = count_evaluation_module.evaluate();

        System.out.println(evaluation_results);
        System.out.println(count_results);


    }
}
