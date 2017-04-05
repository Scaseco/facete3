/**
  * Created by hpetzka on 07.12.2016.
  */
object evaluation_module {
  def apply() {

    // ______________________________________
    // 1.) Instance retrieval SPARQL queries
    // ______________________________________

    // Suppose for each SPARQL query the evaluation module receives a tuple "query_to_evaluate"

    val scenario_id: Int = 1
    val query_id: Int = 1
    val query_result: List[String] = List("<http:/connection1>", "<http:/connection2>", "<http:/connection3>")
    val time_needed: Double = 4.7

    val query_to_evalute: ((Int, Int), List[String], Double) = ((scenario_id, query_id), query_result, time_needed)

    // In this first example, suppose we are given a second query in the first scenario

    val query_id_2: Int = 2
    val query_result_2: List[String] = List("<http:/connection4>", "<http:/connection5>", "<http:/connection5")
    val time_needed_2: Double = 2.3

    val query_to_evalute_2: ((Int, Int), List[String], Double) = ((scenario_id, query_id_2), query_result_2, time_needed_2)

    // Further, assume that we are given a Map which stores for each choke points the list of corresponding queries
    // (Alternatively, one could also have a Map from query to choke points, or store this information in the query-tuple)

    val choke_points_table: Map[Int, List[(Int, Int)]] = Map(1 -> List((1, 1)), 2 -> List((1, 1), (1, 2)), 3 -> List((1, 2)))
    // In this example choke point 1 is tested in szenario 1, query 1
    //                 choke point 2 is tested in szenario 1 query 1 and query 2
    //                 and choke point 3 is tested in szenario 1 query 2

    // ______________________________________
    // 1.0) Preparation:
    // For each result determine
    //      true positives
    //      false positives
    //      false negatives
    // ______________________________________

    // The correct results:
    val gold_standard: ((Int, Int), List[String]) =
      ((scenario_id, query_id), List("<http:/connection1>", "<http:/connection2>", "<http:/connection4>"))
    val gold_standard_2: ((Int, Int), List[String]) =
      ((scenario_id, query_id_2), List("<http:/connection4>", "<http:/connection5>"))

    // True positives: The number two has, of course, to be computed
    val tp: ((Int, Int), Long) = ((scenario_id, query_id), 2L)
    // False positives:  The number one has, of course, to be computed
    val fp: ((Int, Int), Long) = ((scenario_id, query_id), 1L)
    // False negatives: The number one has, of course, to be computed
    val fn: ((Int, Int), Long) = ((scenario_id, query_id), 1L)
    // The number of correct answers
    val p: ((Int, Int), Long) = ((scenario_id, query_id), gold_standard._2.size)


    // Same for query 2
    val tp_2: ((Int, Int), Long) = ((scenario_id, query_id_2), 2L)
    val fp_2: ((Int, Int), Long) = ((scenario_id, query_id_2), 0L)
    val fn_2: ((Int, Int), Long) = ((scenario_id, query_id_2), 1L)
    // since connection5 appears twice in list of results
    val p_2: ((Int, Int), Long) = ((scenario_id, query_id), gold_standard_2._2.size)


    // ______________________________________
    // 1.a) Overall results
    // ______________________________________

    // ______________________________________
    // 1.a.i) Time score in queries per second
    // ______________________________________

    val overall_time_needed = time_needed + time_needed_2
    val number_of_queries = 2

    val query_per_second_score = number_of_queries / overall_time_needed

    // ______________________________________
    // 1.a.ii) Precision, recall, F1-measure
    // ______________________________________

    // Sum tp, fp, fn over all scenarios and all queries
    val overall_tp = tp._2 + tp_2._2
    val overall_fp = fp._2 + fp._2
    val overall_fn = fn._2 + fn_2._2
    val overall_p = p._2 + p_2._2

    // Compute precision, recall and f1-measure
    val overall_precision = overall_tp / overall_p

    val over_all_recall = overall_tp / (overall_tp + overall_fn)

    val overall_f1 = 2 * overall_precision * over_all_recall / (overall_precision + over_all_recall)


    // ______________________________________
    // 1.b) Results per choke point
    // ______________________________________

    // ______________________________________
    // 1.b.i) Time score in queries per second
    // ______________________________________

    // For each choke point, find the queries related to it with help of the choke points table
    // and sum the time needed over all those queries
    // Then for each choke point return the query-per-seconds score as in 1.a.i

    val choke_point_1_time_needed: Double =...
    val choke_point_1_number_of_queries: Int =...over_all_recall

    val choke_point_1_query_per_second_score: Double = choke_point_1_number_of_queries / choke_point_1_time_needed

    // ______________________________________
    // 1.b.ii) Precision, recall, F1-measure per choke point
    // ______________________________________

    // For each choke point independently, compute the three numbers as above


    // ______________________________________
    // 1.c.ii) Precision, recall, F1-measure per Scenario ???
    // ______________________________________

    // Maybe we want to sum over each scenario, too
    // This shouldn't be much more work, but might enable better comparisons of competing systems

    // ______________________________________

    // ______________________________________
    // 2.) COUNT queries
    // ______________________________________

    val count_id = 1
    val count_result: Long = 10L
    val count_time_needed: Double = 4.7

    val count_to_evaluate: ((Int, Int), Long, Double) = ((scenario_id, count_id), count_result, time_needed)


    // ______________________________________
    // 2.a.i) Time score in queries per second
    // ______________________________________

    val count_time_needed = count_time_needed + count_time_needed_2
    val number_of_counts = 2

    val count_per_second_score = number_of_counts / count_time_needed

    // ______________________________________
    // 2.a.ii) Correctness of counts
    // ______________________________________

    val count_gold_standard = ((scenario_id, count_id), 11L)
    val distance_from_correct = ((scenario_id, count_id), Math.abs(count_to_evaluate._2 - count_gold_standard._2))

    val correct_counts = // sum every distance_from_correct over all scenarios and count queries

    val correct_count_ratio = correct_counts / (sum evey count_gold_standard over all scenarios and queries)

    // ______________________________________
    // 2.b.i) Time score in queries per second for each scenario independently
    // ______________________________________


    // ______________________________________
    // 2.b.ii) Correctness of counts for each scenario independently
    // ______________________________________

  }
}




