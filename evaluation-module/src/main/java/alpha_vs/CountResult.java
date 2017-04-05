package alpha_vs;

/**
 * Created by hpetzka on 12.12.2016.
 */
public class CountResult {

    private double count_per_second_score;
    private long overall_error;
    private double average_error;
    private double overall_error_ratio;
    private double average_error_ratio;


    public CountResult(
            double count_per_second_score,
            long overall_error,
            double average_error,
            double overall_error_ratio,
            double average_error_ratio){

        this.count_per_second_score = count_per_second_score;
        this.overall_error = overall_error;
        this.average_error = average_error;
        this.overall_error_ratio = overall_error_ratio;
        this.average_error_ratio = average_error_ratio;
    }

    public double getCount_per_second_score() { return count_per_second_score; }
    public long getOverall_error() {
        return overall_error;
    }
    public double getAverage_error() {
        return average_error;
    }
    public double getOverall_error_ratio() {
        return overall_error_ratio;
    }
    public double getAverage_error_ratio() {
        return average_error_ratio;
    }



    public String toString()
    {
        return "The count results are as follows: \n \n" +
                "Count per second score = "+ this.count_per_second_score+"\n"+
                "Overall error = "+this.overall_error+"\n"+
                "Average_error = "+this.average_error+"\n"+
                "Overall error ratio = "+this.overall_error_ratio+"\n"+
                "Average error ratio = "+this.average_error_ratio+"\n";
    }

}
