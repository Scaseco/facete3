package org.hobbit.evaluation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hpetzka on 14.12.2016.
 */
public class ChokePoints {
    public static final HashMap<Integer, ArrayList<QueryID>> chokePointsTable = new HashMap<>(); 

    static {
        ArrayList<QueryID> choke_list_1 = new ArrayList<>();
        choke_list_1.add(new QueryID(5, 3));
        choke_list_1.add(new QueryID(7, 1));
        chokePointsTable.put(1, choke_list_1);

        ArrayList<QueryID> choke_list_2 = new ArrayList<>();
        choke_list_2.add(new QueryID(2, 1));
        choke_list_2.add(new QueryID(3, 3));
        choke_list_2.add(new QueryID(7, 4));
        choke_list_2.add(new QueryID(8, 3));
        chokePointsTable.put(2, choke_list_2);

        ArrayList<QueryID> choke_list_3 = new ArrayList<>();
        choke_list_3.add(new QueryID(1, 8));
        choke_list_3.add(new QueryID(1, 10));
        choke_list_3.add(new QueryID(3, 2));
        choke_list_3.add(new QueryID(4, 9));
        choke_list_3.add(new QueryID(10, 7));
        choke_list_3.add(new QueryID(10, 10));
        chokePointsTable.put(3, choke_list_3);

        ArrayList<QueryID> choke_list_4 = new ArrayList<>();
        choke_list_4.add(new QueryID(2, 2));
        choke_list_4.add(new QueryID(3, 4));
        choke_list_4.add(new QueryID(3, 8));
        choke_list_4.add(new QueryID(4, 5));
        choke_list_4.add(new QueryID(4, 10));
        choke_list_4.add(new QueryID(5, 6));
        choke_list_4.add(new QueryID(5, 8));
        choke_list_4.add(new QueryID(6, 4));
        choke_list_4.add(new QueryID(7, 5));
        choke_list_4.add(new QueryID(7, 7));
        choke_list_4.add(new QueryID(8, 4));
        choke_list_4.add(new QueryID(8, 6));
        choke_list_4.add(new QueryID(9, 1));
        choke_list_4.add(new QueryID(9, 6));
        choke_list_4.add(new QueryID(11, 1));
        chokePointsTable.put(4, choke_list_4);

        ArrayList<QueryID> choke_list_5 = new ArrayList<>();
        choke_list_5.add(new QueryID(2, 3));
        choke_list_5.add(new QueryID(2, 5));
        choke_list_5.add(new QueryID(2, 8));
        choke_list_5.add(new QueryID(3, 9));
        choke_list_5.add(new QueryID(8, 10));
        chokePointsTable.put(5, choke_list_5);

        ArrayList<QueryID> choke_list_6 = new ArrayList<>();
        choke_list_6.add(new QueryID(1, 2));
        choke_list_6.add(new QueryID(1, 6));
        choke_list_6.add(new QueryID(2, 6));
        choke_list_6.add(new QueryID(2, 9));
        choke_list_6.add(new QueryID(2, 10));
        choke_list_6.add(new QueryID(2, 11));
        choke_list_6.add(new QueryID(4, 3));
        choke_list_6.add(new QueryID(5, 4));
        choke_list_6.add(new QueryID(5, 5));
        choke_list_6.add(new QueryID(5, 9));
        choke_list_6.add(new QueryID(8, 8));
        choke_list_6.add(new QueryID(9, 3));
        choke_list_6.add(new QueryID(9, 7));
        chokePointsTable.put(6, choke_list_6);

        ArrayList<QueryID> choke_list_7 = new ArrayList<>();
        choke_list_7.add(new QueryID(1, 1));
        choke_list_7.add(new QueryID(1, 3));
        choke_list_7.add(new QueryID(1, 4));
        choke_list_7.add(new QueryID(1, 5));
        choke_list_7.add(new QueryID(1, 7));
        choke_list_7.add(new QueryID(3, 1));
        choke_list_7.add(new QueryID(3, 6));
        choke_list_7.add(new QueryID(3, 7));
        choke_list_7.add(new QueryID(3, 10));
        choke_list_7.add(new QueryID(3, 11));
        choke_list_7.add(new QueryID(4, 1));
        choke_list_7.add(new QueryID(4, 2));
        choke_list_7.add(new QueryID(4, 4));
        choke_list_7.add(new QueryID(4, 7));
        choke_list_7.add(new QueryID(4, 8));
        choke_list_7.add(new QueryID(5, 1));
        choke_list_7.add(new QueryID(5, 2));
        choke_list_7.add(new QueryID(9, 9));
        choke_list_7.add(new QueryID(9, 10));
        chokePointsTable.put(7, choke_list_7);

        ArrayList<QueryID> choke_list_8 = new ArrayList<>();
        choke_list_8.add(new QueryID(1, 2));
        choke_list_8.add(new QueryID(1, 3));
        choke_list_8.add(new QueryID(1, 4));
        choke_list_8.add(new QueryID(1, 5));
        choke_list_8.add(new QueryID(1, 6));
        choke_list_8.add(new QueryID(1, 7));
        choke_list_8.add(new QueryID(3, 6));
        choke_list_8.add(new QueryID(3, 7));
        choke_list_8.add(new QueryID(3, 10));
        choke_list_8.add(new QueryID(3, 11));
        choke_list_8.add(new QueryID(4, 3));
        choke_list_8.add(new QueryID(4, 4));
        choke_list_8.add(new QueryID(4, 7));
        choke_list_8.add(new QueryID(4, 8));
        choke_list_8.add(new QueryID(5, 4));
        choke_list_8.add(new QueryID(5, 5));
        choke_list_8.add(new QueryID(5, 9));
        choke_list_8.add(new QueryID(6, 3));
        choke_list_8.add(new QueryID(6, 5));
        choke_list_8.add(new QueryID(6, 6));
        choke_list_8.add(new QueryID(6, 7));
        choke_list_8.add(new QueryID(6, 8));
        choke_list_8.add(new QueryID(6, 9));
        choke_list_8.add(new QueryID(8, 8));
        choke_list_8.add(new QueryID(9, 3));
        choke_list_8.add(new QueryID(9, 9));
        chokePointsTable.put(8, choke_list_8);

        ArrayList<QueryID> choke_list_9 = new ArrayList<>();
        choke_list_9.add(new QueryID(1, 2));
        choke_list_9.add(new QueryID(3, 6));
        choke_list_9.add(new QueryID(3, 7));
        choke_list_9.add(new QueryID(3, 10));
        choke_list_9.add(new QueryID(3, 11));
        choke_list_9.add(new QueryID(5, 4));
        choke_list_9.add(new QueryID(6, 6));
        choke_list_9.add(new QueryID(9, 3));
        choke_list_9.add(new QueryID(9, 7));
        chokePointsTable.put(9, choke_list_9);

        ArrayList<QueryID> choke_list_10 = new ArrayList<>();
        choke_list_10.add(new QueryID(1, 9));
        choke_list_10.add(new QueryID(2, 4));
        choke_list_10.add(new QueryID(2, 7));
        choke_list_10.add(new QueryID(3, 5));
        choke_list_10.add(new QueryID(4, 6));
        choke_list_10.add(new QueryID(5, 7));
        choke_list_10.add(new QueryID(7, 6));
        choke_list_10.add(new QueryID(7, 8));
        choke_list_10.add(new QueryID(8, 5));
        choke_list_10.add(new QueryID(8, 9));
        choke_list_10.add(new QueryID(9, 5));
        choke_list_10.add(new QueryID(9, 10));
        choke_list_10.add(new QueryID(10, 4));
        choke_list_10.add(new QueryID(10, 8));
        choke_list_10.add(new QueryID(10, 9));
        choke_list_10.add(new QueryID(11, 4));
        choke_list_10.add(new QueryID(11, 6));
        choke_list_10.add(new QueryID(11, 7));
        chokePointsTable.put(10, choke_list_10);

        ArrayList<QueryID> choke_list_11 = new ArrayList<>();
        choke_list_11.add(new QueryID(8, 7));
        choke_list_11.add(new QueryID(9, 8));
        choke_list_11.add(new QueryID(10, 6));
        chokePointsTable.put(11, choke_list_11);

        ArrayList<QueryID> choke_list_12 = new ArrayList<>();
        choke_list_12.add(new QueryID(11, 5));
        choke_list_12.add(new QueryID(11, 8));
        choke_list_12.add(new QueryID(11, 9));
        chokePointsTable.put(12, choke_list_12);

        ArrayList<QueryID> choke_list_13 = new ArrayList<>();
        choke_list_13.add(new QueryID(6, 4));
        choke_list_13.add(new QueryID(6, 10));
        choke_list_13.add(new QueryID(7, 4));
        choke_list_13.add(new QueryID(7, 5));
        choke_list_13.add(new QueryID(7, 7));
        choke_list_13.add(new QueryID(8, 2));
        choke_list_13.add(new QueryID(10, 3));
        choke_list_13.add(new QueryID(10, 5));
        choke_list_13.add(new QueryID(11, 3));
        chokePointsTable.put(13, choke_list_13);

        ArrayList<QueryID> choke_list_14 = new ArrayList<>();
        choke_list_14.add(new QueryID(6, 1));
        choke_list_14.add(new QueryID(6, 2));
        choke_list_14.add(new QueryID(6, 3));
        choke_list_14.add(new QueryID(6, 5));
        choke_list_14.add(new QueryID(6, 6));
        choke_list_14.add(new QueryID(6, 7));
        choke_list_14.add(new QueryID(6, 8));
        choke_list_14.add(new QueryID(6, 9));
        choke_list_14.add(new QueryID(7, 2));
        choke_list_14.add(new QueryID(7, 3));
        choke_list_14.add(new QueryID(8, 1));
        choke_list_14.add(new QueryID(9, 2));
        choke_list_14.add(new QueryID(9, 4));
        choke_list_14.add(new QueryID(10, 1));
        choke_list_14.add(new QueryID(10, 2));
        choke_list_14.add(new QueryID(11, 2));
        chokePointsTable.put(14, choke_list_14);
        
    }

    public static HashMap<Integer, ArrayList<QueryID>> getTable() {
        return chokePointsTable;
    }
}





