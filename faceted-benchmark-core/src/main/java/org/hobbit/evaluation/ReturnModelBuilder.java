package org.hobbit.evaluation;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;


/**
 * Created by hpetzka on 14.12.2016.
 */
public class ReturnModelBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnModelBuilder.class);
    public ReturnModelBuilder() {
    }

    public static Model build(String experimentUri,
                              double overall_query_per_second_score,
                              double overall_precision,
                              double overall_recall,
                              double overall_f1,
                              HashMap<Integer, Double> chokePT_query_per_second_score,
                              HashMap<Integer, Double> chokePT_precision,
                              HashMap<Integer, Double> chokePT_recall,
                              HashMap<Integer, Double> chokePT_f1,
                              long overall_error,
                              double average_error,
                              double overall_error_ratio,
                              double average_error_ratio,
                              double count_per_second_score,
                              ArrayList<QueryID> queriesWithTimeout){
        Model rdfModel = createDefaultModel();

        Resource experimentResource = experimentUri == null ? rdfModel.createResource() : rdfModel.createResource(experimentUri);
        rdfModel.add(experimentResource , RDF.type, HOBBIT.Experiment);

        String rdfInTTL ="@prefix ex: <http://example.org/> .\n" +
                "@prefix hobbit: <http://w3id.org/hobbit/vocab#> .\n" +
                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix bench: <http://w3id.org/bench#> .\n" +
                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n \n";

        /* APPARENTLY NOT NEEDED, AUTOMATICALLY ADDED
            String expInfo = String.format("%s ;"
                , experimentUri,
                HOBBIT.System.getURI(),
                HOBBIT.Benchmark.getURI(),
                HOBBIT.Hardware.getURI(),
                HOBBIT.Challenge.getURI()
        );

        rdfInTTL = rdfInTTL.concat(expInfo);
        */

        String overallResults = String.format(""
                +"<%s> \t bench:precision_total \" %f\"^^xsd:float ; \n"
                +"\t bench:recall_total \"%f\"^^xsd:float ; \n"
                +"\t bench:fmeasure_total \"%f\"^^xsd:float ; \n"
                +"\t bench:query_per_second_score_total \"%f\"^^xsd:float ",
                experimentUri, overall_precision, overall_recall, overall_f1, overall_query_per_second_score);

        rdfInTTL = rdfInTTL.concat(overallResults);

        LOGGER.info("Concatenated rdf: "+ rdfInTTL);

        for(int i=1; i<=14; i++){

            String chokePTResults = String.format(" ; \n"
                    +" \t bench:precision_choke_point%d \" %f\"^^xsd:float ; \n"
                    +"\t bench:recall_choke_point%d \"%f\"^^xsd:float ; \n"
                    +"\t bench:fmeasure_choke_point%d \"%f\"^^xsd:float ; \n"
                    +"\t bench:query_per_second_score_choke_point%d \"%f\"^^xsd:float ",
                    i , chokePT_precision.get(i) , i , chokePT_recall.get(i) , i , chokePT_f1.get(i) , i , chokePT_query_per_second_score.get(i));
            rdfInTTL = rdfInTTL.concat(chokePTResults);
        }

        String countResults = String.format(" ; \n"
                +"\t bench:count_overall_error \"%d\"^^xsd:long ; \n"
                +"\t bench:count_average_error \"%f\"^^xsd:float ;  \n"
                +"\t bench:count_overall_error_ratio \"%f\"^^xsd:float ; \n"
                +"\t bench:count_average_error_ratio \"%f\"^^xsd:float ; \n"
                +"\t bench:count_per_second_score \"%f\"^^xsd:float"
        , overall_error, average_error, overall_error_ratio, average_error_ratio, count_per_second_score);

        rdfInTTL = rdfInTTL.concat(countResults);

        String timeoutResults= " ; \n \t bench:timeoutOn \"";
        HashSet<String> timeoutResultsSet= new HashSet<String>(){};

        boolean[] incompleteScenarios = new boolean[12];
        String incompleteScenariosStr = " ; \n \t bench:UncompletedScenarios \"";
        HashSet<String> incompleteScenariosStrSet = new HashSet<String>(){};

        if (queriesWithTimeout.isEmpty()) {
            timeoutResults = timeoutResults.concat("none");
            incompleteScenariosStr = incompleteScenariosStr.concat("none");
        }
        else {
            for (QueryID x : queriesWithTimeout) {
                timeoutResultsSet.add(x.toString());
                byte i = x.getScenario();
                incompleteScenarios[i]=true ;
            }


            for (int i=0; i<= 11; i++) {
                if (incompleteScenarios[i] == true) {
                    if (i==0) {
                        incompleteScenariosStrSet.add("Counts");
                    }
                    else incompleteScenariosStrSet.add(""+i);
                }
            }
            timeoutResults = timeoutResults.concat(timeoutResultsSet.toString());
            incompleteScenariosStr = incompleteScenariosStr.concat(incompleteScenariosStrSet.toString());
        }

        timeoutResults = timeoutResults.concat("\"^^xsd:string");
        incompleteScenariosStr = incompleteScenariosStr.concat("\"^^xsd:string");

        rdfInTTL = rdfInTTL.concat(timeoutResults);
        rdfInTTL = rdfInTTL.concat(incompleteScenariosStr);

        String ChokePtsWithTimeOut = " ; \n \t bench:ProblemsOnChokePoints \"";
        HashSet ChokePtsWithTimeOutSet = new HashSet(){};

        HashMap<Integer, ArrayList<QueryID>> chokePointsTable = ChokePoints.getTable();



        for (Map.Entry<Integer, ArrayList<QueryID>> entry : chokePointsTable.entrySet()) {
            for (QueryID x : entry.getValue()){
                if(queriesWithTimeout.contains(x)) {
                    ChokePtsWithTimeOutSet.add(entry.getKey());
                }
            }
        }

        ChokePtsWithTimeOut = ChokePtsWithTimeOut.concat(ChokePtsWithTimeOutSet.toString()+"\"^^xsd:string");

        rdfInTTL = rdfInTTL.concat(ChokePtsWithTimeOut);

        rdfInTTL = rdfInTTL.concat(" .");

        LOGGER.info("Final rdf: "+ rdfInTTL);
        rdfModel.read(new ByteArrayInputStream(rdfInTTL.getBytes()), null, "TTL" );
        return rdfModel;
    }


}
