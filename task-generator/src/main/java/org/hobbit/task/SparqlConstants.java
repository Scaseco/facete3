package org.hobbit.task;

/**
 * Created by gkatsimpras on 19/12/2016.
 */
public final class SparqlConstants {
    //Hide the constructor
    private SparqlConstants(){}

    public static String QUERY_111 = "asdf";

    public static String QUERY_1 = "PREFIX lc: <http://semweb.mmlab.be/ns/linkedconnections#>\n" +
            "SELECT DISTINCT ?connection ?lat ?long\n" +
            " WHERE {\n" +
            "  ?connection lc:departureStop ?stop .\n" +
            "  ?stop <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat ;\n" +
            "      <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . \n" +
            "  FILTER(((\"0.3\" < ?lat) && (?lat < \"1.7\") && (\"0.3\" < ?long) && (?long < \"1.7\") ) )\n" +
            "  }";

    public static String QUERY_2 = "query2";

}
