package alpha_vs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hpetzka on 12.12.2016.
 */
public class CountEvaluationModule {

        private HashMap<Integer,Pair<Long,Double>> results;
        private HashMap<Integer,Long> golds;

        public CountEvaluationModule(HashMap<Integer,Pair<Long,Double>> results,
                                     HashMap<Integer,Long> golds) {

            this.results = results;
            this.golds = golds;
        }


        public CountResult evaluate() {

            // ______________________________________
            // 2.) COUNT queries
            // ______________________________________


            // ______________________________________
            // 2.0) Preparation
            // ______________________________________



            double count_time_needed = 0;
            int number_of_counts = 0;
            long error = 0L;
            double error_ratio =0;
            long sum_of_correct_count_results =0L;

            Set<Map.Entry<Integer,Pair<Long, Double> >> entries = this.results.entrySet();


            for (Map.Entry<Integer,Pair<Long , Double> > entry : entries) {

                number_of_counts += 1;

                int key = entry.getKey();

                Pair<Long, Double> result = entry.getValue();
                Long count_result = result.getFirst();
                Long gold = golds.get(key);

                count_time_needed += result.getSecond();

                long current_error = Math.abs(count_result - gold);
                error += current_error;
                error_ratio += (double) current_error / gold;

            }


            // ______________________________________
            // 2.a.i) Time score in queries per second
            // ______________________________________


            double count_per_second_score = number_of_counts / count_time_needed;

            // ______________________________________
            // 2.a.ii) Correctness of counts
            // ______________________________________

            long overall_error = error;
            double average_error = (double) error/number_of_counts;
            double overall_error_ratio =(double)  error/sum_of_correct_count_results;
            double average_error_ratio =  error_ratio/number_of_counts;



            // ______________________________________
            // 2.b.i) Time score in queries per second for each scenario independently ???
            // ______________________________________


            // ______________________________________
            // 2.b.ii) Correctness of counts for each scenario independently ???
            // ______________________________________

        return new CountResult(count_per_second_score, overall_error, average_error, overall_error_ratio, average_error_ratio);

        }


}
