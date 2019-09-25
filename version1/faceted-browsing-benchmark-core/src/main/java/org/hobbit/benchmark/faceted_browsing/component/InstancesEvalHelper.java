package org.hobbit.benchmark.faceted_browsing.component;

/**
 * Created by hpetzka on 14.12.2016.
 */
public class InstancesEvalHelper {

    Integer true_pos;
    Integer false_neg;
    Integer false_pos;
    Integer query_time;

    InstancesEvalHelper(Integer true_pos, Integer false_neg, Integer false_pos, Integer query_time){
        this.true_pos = true_pos;
        this.false_neg = false_neg;
        this.false_pos = false_pos;
        this.query_time = query_time;

    }

    public int hashCode() {
        int hashTP = true_pos != null ? true_pos.hashCode() : 0;
        int hashFN = true_pos != null ? true_pos.hashCode() : 0;
        int hashFP = true_pos != null ? true_pos.hashCode() : 0;
        int hashQT = true_pos != null ? true_pos.hashCode() : 0;

        return hashTP * hashFN*hashFP *hashQT + hashFN * hashFP *hashQT + hashFP * hashQT + hashQT;
    }


    public boolean equals(Object other) {
        if (other instanceof InstancesEvalHelper) {
            InstancesEvalHelper otherPair = (InstancesEvalHelper) other;
            return
                    (( this.true_pos == otherPair.true_pos ||
                            ( this.true_pos != null && otherPair.true_pos != null &&
                                    this.true_pos.equals(otherPair.true_pos))) &&
                            ( this.false_neg == otherPair.false_neg )||
                            ( this.false_neg != null && otherPair.false_neg != null &&
                                    this.false_neg.equals(otherPair.false_neg)) &&
                                    ( this.false_pos == otherPair.false_pos )||
                            ( this.false_pos != null && otherPair.false_pos != null &&
                                    this.false_pos.equals(otherPair.false_pos)) &&
                                    ( this.query_time == otherPair.query_time )||
                            ( this.query_time != null && otherPair.query_time != null &&
                                    this.query_time.equals(otherPair.query_time)));
        }
        return false;
    }


    public String toString()
    {
        return "( True positives, False negatives, False positives, Time ) = ("+  true_pos + ", " + false_neg +
                ", " + false_pos +", " + query_time + ")";
    }


    public Integer getTP() {
        return true_pos;
    }

    public Integer getFN() {
        return false_neg;
    }

    public Integer getFP() {
        return false_pos;
    }

    public Integer getTime() {
        return query_time;
    }

    public void add(InstancesEvalHelper other){
        this.true_pos += other.getTP();
        this.false_neg += other.getFN();
        this.false_pos += other.getFP();
        this.query_time += other.getTime();
    }

    public void add(int tp, int fn, int fp, int time){
        this.true_pos += tp;
        this.false_neg += fn;
        this.false_pos += fp;
        this.query_time += time;
    }

}
