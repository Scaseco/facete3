package alpha_vs;


import java.util.HashMap;

/**
 * Created by hpetzka on 09.12.2016.
 */
public class EvaluationResult {

    private double overall_query_per_second_score;
    private double overall_precision;
    private double overall_recall;
    private double overall_f1;

    private HashMap<Integer,Double> chokePT_query_per_second_score;
    private HashMap<Integer,Double> chokePT_precision;
    private HashMap<Integer,Double> chokePT_recall;
    private HashMap<Integer,Double> chokePT_f1;

    public EvaluationResult(double overall_query_per_second_score,
                            double overall_precision,
                            double overall_recall,
                            double overall_f1,

                            HashMap<Integer,Double> chokePT_query_per_second_score,
                            HashMap<Integer,Double> chokePT_precision,
                            HashMap<Integer,Double> chokePT_recall,
                            HashMap<Integer,Double> chokePT_f1){

        this.overall_query_per_second_score = overall_query_per_second_score;
        this.overall_precision = overall_precision;
        this.overall_recall = overall_recall;
        this.overall_f1 = overall_f1;

        this.chokePT_query_per_second_score = chokePT_query_per_second_score;
        this.chokePT_precision = chokePT_precision;
        this.chokePT_recall = chokePT_recall;
        this.chokePT_f1 = chokePT_f1;
    }

    public double getOverall_query_per_second_score() {
        return overall_query_per_second_score;
    }
    public double getOverall_precision() {
        return overall_precision;
    }
    public double getOverall_recall() {
        return overall_recall;
    }
    public double getOverall_f1() {
        return overall_f1;
    }


    public HashMap<Integer,Double> getChokePT_query_per_second_score() {
        return chokePT_query_per_second_score;
    }
    public HashMap<Integer,Double> getChokePT_precision() {
        return chokePT_precision;
    }
    public HashMap<Integer,Double> getChokePT_recall() {
        return chokePT_recall;
    }
    public HashMap<Integer,Double> getChokePT_f1() {
        return chokePT_f1;
    }

    public String toString()
    {
        return "The evaluation results are as follows: \n \n" +
                "Overall_query_per_second_score = "+this.overall_query_per_second_score+"\n"+
                "Overall_precision = "+this.overall_precision+"\n"+
                "Overall_recall = "+this.overall_recall+"\n"+
                "Overall_f1 = "+this.overall_f1+"\n"+
                "\n" +
                "chokePT_query_per_second_score = "+this.chokePT_query_per_second_score+"\n"+
                "chokePT_precision = "+this.chokePT_precision+"\n"+
                "chokePT_recall = "+this.chokePT_recall+"\n"+
                "chokePT_f1 = "+chokePT_f1+"\n" ;
    }

}
