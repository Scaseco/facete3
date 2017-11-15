package test;

import org.hobbit.evaluation.QueryID;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hpetzka on 14.12.2016.
 */
public class ChokePointsTest {
    public static final HashMap<Integer, ArrayList<QueryIDTest>> chokePointsTable = new HashMap<>();

    static {
        ArrayList<QueryIDTest> choke_list_1 = new ArrayList<>();
        choke_list_1.add(new QueryIDTest(1, 1));
        chokePointsTable.put(1, choke_list_1);

        ArrayList<QueryIDTest> choke_list_2 = new ArrayList<>();
        choke_list_2.add(new QueryIDTest(1, 1));
        choke_list_2.add(new QueryIDTest(1, 2));
        chokePointsTable.put(2, choke_list_2);

        ArrayList<QueryIDTest> choke_list_3 = new ArrayList<>();
        choke_list_3.add(new QueryIDTest(2, 1));
        choke_list_3.add(new QueryIDTest(2, 2));
        chokePointsTable.put(3, choke_list_3);
    }

    public static HashMap<Integer, ArrayList<QueryIDTest>> getTable() {
        return chokePointsTable;
    }
}

