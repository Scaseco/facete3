package org.hobbit.benchmark.faceted_browsing.v1.evaluation;

import java.util.ArrayList;

/**
 * Created by hpetzka on 14.12.2016.
 */
public class ResultPair {
    
    private QueryID key;
    private ArrayList<String> data;

    public ResultPair(QueryID key, ArrayList<String> data) {
        super();
        this.key = key;
        this.data = data;
    }

    public int hashCode() {
        int hashkey = key != null ? key.hashCode() : 0;
        int hashdata = data != null ? data.hashCode() : 0;

        return (hashkey + hashdata) * hashdata + hashkey;
    }

    public boolean equals(Object other) {
        if (other instanceof ResultPair) {
            ResultPair otherPair = (ResultPair) other;
            return
                    ((  this.key == otherPair.key ||
                            ( this.key != null && otherPair.key != null &&
                                    this.key.equals(otherPair.key))) &&
                            (  this.data == otherPair.data ||
                                    ( this.data != null && otherPair.data != null &&
                                            this.data.equals(otherPair.data))) );
        }

        return false;
    }

    public String toString()
    {
        return "(" + key + ", " + data + ")";
    }

    public QueryID getKey() {
        return key;
    }


    public ArrayList<String> getData() {
        return data;
    }

}
