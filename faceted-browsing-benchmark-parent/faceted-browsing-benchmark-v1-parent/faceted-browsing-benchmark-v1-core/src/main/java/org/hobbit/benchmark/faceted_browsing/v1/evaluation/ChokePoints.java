package org.hobbit.benchmark.faceted_browsing.v1.evaluation;

import java.util.ArrayList;
import java.util.HashMap;

import org.hobbit.benchmark.faceted_browsing.component.QueryID;

/**
 * Created by hpetzka on 14.12.2016.
 */
public class ChokePoints {
    public static final HashMap<Integer, ArrayList<QueryID>> chokePointsTable = new HashMap<>(); 

    static {
        ArrayList<QueryID> choke_list_1 = new ArrayList<>();
        choke_list_1.add(new QueryID(5, 8));
        choke_list_1.add(new QueryID(7, 1));
        chokePointsTable.put(1, choke_list_1);

        ArrayList<QueryID> choke_list_2 = new ArrayList<>();
        choke_list_2.add(new QueryID(2, 1));
        choke_list_2.add(new QueryID(3, 8));
        choke_list_2.add(new QueryID(7, 10));
        choke_list_2.add(new QueryID(8, 8));
        chokePointsTable.put(2, choke_list_2);

        ArrayList<QueryID> choke_list_3 = new ArrayList<>();
        choke_list_3.add(new QueryID(1, 14));
        choke_list_3.add(new QueryID(1, 16));
        choke_list_3.add(new QueryID(3, 5));
        choke_list_3.add(new QueryID(4, 15));
        choke_list_3.add(new QueryID(10, 13));
        choke_list_3.add(new QueryID(10, 16));
        chokePointsTable.put(3, choke_list_3);

        ArrayList<QueryID> choke_list_4 = new ArrayList<>();
        choke_list_4.add(new QueryID(2, 5));
        choke_list_4.add(new QueryID(3, 10));
        choke_list_4.add(new QueryID(3, 14));
        choke_list_4.add(new QueryID(4, 11));
        choke_list_4.add(new QueryID(4, 16));
        choke_list_4.add(new QueryID(5, 12));
        choke_list_4.add(new QueryID(5, 14));
        choke_list_4.add(new QueryID(6, 10));
        choke_list_4.add(new QueryID(7, 11));
        choke_list_4.add(new QueryID(7, 13));
        choke_list_4.add(new QueryID(8, 10));
        choke_list_4.add(new QueryID(8, 12));
        choke_list_4.add(new QueryID(9, 1));
        choke_list_4.add(new QueryID(9, 12));
        choke_list_4.add(new QueryID(11, 1));
        chokePointsTable.put(4, choke_list_4);

        ArrayList<QueryID> choke_list_5 = new ArrayList<>();
        choke_list_5.add(new QueryID(2, 8));
        choke_list_5.add(new QueryID(2, 11));
        choke_list_5.add(new QueryID(2, 14));
        choke_list_5.add(new QueryID(3, 15));
        choke_list_5.add(new QueryID(8, 16));
        chokePointsTable.put(5, choke_list_5);

        ArrayList<QueryID> choke_list_6 = new ArrayList<>();
        choke_list_6.add(new QueryID(1, 5));
        choke_list_6.add(new QueryID(1, 12));
        choke_list_6.add(new QueryID(2, 12));
        choke_list_6.add(new QueryID(2, 15));
        choke_list_6.add(new QueryID(2, 16));
        choke_list_6.add(new QueryID(2, 17));
        choke_list_6.add(new QueryID(4, 8));
        choke_list_6.add(new QueryID(5, 10));
        choke_list_6.add(new QueryID(5, 11));
        choke_list_6.add(new QueryID(5, 15));
        choke_list_6.add(new QueryID(8, 14));
        choke_list_6.add(new QueryID(9, 8));
        choke_list_6.add(new QueryID(9, 13));
        chokePointsTable.put(6, choke_list_6);

        ArrayList<QueryID> choke_list_7 = new ArrayList<>();
        choke_list_7.add(new QueryID(1, 1));
        choke_list_7.add(new QueryID(1, 8));
        choke_list_7.add(new QueryID(1, 10));
        choke_list_7.add(new QueryID(1, 11));
        choke_list_7.add(new QueryID(1, 13));
        choke_list_7.add(new QueryID(3, 1));
        choke_list_7.add(new QueryID(3, 12));
        choke_list_7.add(new QueryID(3, 13));
        choke_list_7.add(new QueryID(3, 16));
        choke_list_7.add(new QueryID(3, 17));
        choke_list_7.add(new QueryID(4, 1));
        choke_list_7.add(new QueryID(4, 5));
        choke_list_7.add(new QueryID(4, 10));
        choke_list_7.add(new QueryID(4, 13));
        choke_list_7.add(new QueryID(4, 14));
        choke_list_7.add(new QueryID(5, 1));
        choke_list_7.add(new QueryID(5, 5));
        choke_list_7.add(new QueryID(9, 15));
        choke_list_7.add(new QueryID(9, 16));
        chokePointsTable.put(7, choke_list_7);

        ArrayList<QueryID> choke_list_8 = new ArrayList<>();
        choke_list_8.add(new QueryID(1, 5));
        choke_list_8.add(new QueryID(1, 8));
        choke_list_8.add(new QueryID(1, 10));
        choke_list_8.add(new QueryID(1, 11));
        choke_list_8.add(new QueryID(1, 12));
        choke_list_8.add(new QueryID(1, 13));
        choke_list_8.add(new QueryID(3, 12));
        choke_list_8.add(new QueryID(3, 13));
        choke_list_8.add(new QueryID(3, 16));
        choke_list_8.add(new QueryID(3, 17));
        choke_list_8.add(new QueryID(4, 8));
        choke_list_8.add(new QueryID(4, 10));
        choke_list_8.add(new QueryID(4, 13));
        choke_list_8.add(new QueryID(4, 14));
        choke_list_8.add(new QueryID(5, 10));
        choke_list_8.add(new QueryID(5, 11));
        choke_list_8.add(new QueryID(5, 15));
        choke_list_8.add(new QueryID(6, 8));
        choke_list_8.add(new QueryID(6, 11));
        choke_list_8.add(new QueryID(6, 12));
        choke_list_8.add(new QueryID(6, 13));
        choke_list_8.add(new QueryID(6, 14));
        choke_list_8.add(new QueryID(6, 15));
        choke_list_8.add(new QueryID(8, 14));
        choke_list_8.add(new QueryID(9, 8));
        choke_list_8.add(new QueryID(9, 15));
        chokePointsTable.put(8, choke_list_8);

        ArrayList<QueryID> choke_list_9 = new ArrayList<>();
        choke_list_9.add(new QueryID(1, 5));
        choke_list_9.add(new QueryID(3, 12));
        choke_list_9.add(new QueryID(3, 13));
        choke_list_9.add(new QueryID(3, 16));
        choke_list_9.add(new QueryID(3, 17));
        choke_list_9.add(new QueryID(5, 10));
        choke_list_9.add(new QueryID(6, 12));
        choke_list_9.add(new QueryID(9, 8));
        choke_list_9.add(new QueryID(9, 13));
        chokePointsTable.put(9, choke_list_9);

        ArrayList<QueryID> choke_list_10 = new ArrayList<>();
        choke_list_10.add(new QueryID(1, 15));
        choke_list_10.add(new QueryID(2, 10));
        choke_list_10.add(new QueryID(2, 13));
        choke_list_10.add(new QueryID(3, 11));
        choke_list_10.add(new QueryID(4, 12));
        choke_list_10.add(new QueryID(5, 13));
        choke_list_10.add(new QueryID(7, 12));
        choke_list_10.add(new QueryID(7, 14));
        choke_list_10.add(new QueryID(8, 11));
        choke_list_10.add(new QueryID(8, 15));
        choke_list_10.add(new QueryID(9, 11));
        choke_list_10.add(new QueryID(9, 16));
        choke_list_10.add(new QueryID(10, 10));
        choke_list_10.add(new QueryID(10, 14));
        choke_list_10.add(new QueryID(10, 15));
        choke_list_10.add(new QueryID(11, 10));
        choke_list_10.add(new QueryID(11, 12));
        choke_list_10.add(new QueryID(11, 13));
        chokePointsTable.put(10, choke_list_10);

        ArrayList<QueryID> choke_list_11 = new ArrayList<>();
        choke_list_11.add(new QueryID(8, 13));
        choke_list_11.add(new QueryID(9, 14));
        choke_list_11.add(new QueryID(10, 12));
        chokePointsTable.put(11, choke_list_11);

        ArrayList<QueryID> choke_list_12 = new ArrayList<>();
        choke_list_12.add(new QueryID(11, 11));
        choke_list_12.add(new QueryID(11, 14));
        choke_list_12.add(new QueryID(11, 15));
        chokePointsTable.put(12, choke_list_12);

        ArrayList<QueryID> choke_list_13 = new ArrayList<>();
        choke_list_13.add(new QueryID(6, 10));
        choke_list_13.add(new QueryID(6, 16));
        choke_list_13.add(new QueryID(7, 10));
        choke_list_13.add(new QueryID(7, 11));
        choke_list_13.add(new QueryID(7, 13));
        choke_list_13.add(new QueryID(8, 5));
        choke_list_13.add(new QueryID(10, 8));
        choke_list_13.add(new QueryID(10, 11));
        choke_list_13.add(new QueryID(11, 8));
        chokePointsTable.put(13, choke_list_13);

        ArrayList<QueryID> choke_list_14 = new ArrayList<>();
        choke_list_14.add(new QueryID(6, 1));
        choke_list_14.add(new QueryID(6, 5));
        choke_list_14.add(new QueryID(6, 8));
        choke_list_14.add(new QueryID(6, 11));
        choke_list_14.add(new QueryID(6, 12));
        choke_list_14.add(new QueryID(6, 13));
        choke_list_14.add(new QueryID(6, 14));
        choke_list_14.add(new QueryID(6, 15));
        choke_list_14.add(new QueryID(7, 5));
        choke_list_14.add(new QueryID(7, 8));
        choke_list_14.add(new QueryID(8, 1));
        choke_list_14.add(new QueryID(9, 5));
        choke_list_14.add(new QueryID(9, 10));
        choke_list_14.add(new QueryID(10, 1));
        choke_list_14.add(new QueryID(10, 5));
        choke_list_14.add(new QueryID(11, 5));
        chokePointsTable.put(14, choke_list_14);
        
    }

    public static HashMap<Integer, ArrayList<QueryID>> getTable() {
        return chokePointsTable;
    }
}





