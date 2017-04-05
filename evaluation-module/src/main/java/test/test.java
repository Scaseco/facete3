package test;


import org.hobbit.core.rabbit.RabbitMQUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hobbit.core.rabbit.RabbitMQUtils.readString;
import static org.hobbit.core.rabbit.RabbitMQUtils.writeByteArrays;
import static org.hobbit.core.rabbit.RabbitMQUtils.writeString;

public class test {

    public static void main (String[] args) throws Exception{


        // The following lines successfully test the writing and reading of byte arrays

        /* byte[] taskID_ = {1,5,3,2};


        String s = "hello , world";
        byte[] data_ = writeString(s);

        byte[][] arrays = {taskID_ ,new byte[]{3,4}, data_ };

        byte[] encrypt = writeByteArrays(arrays);

        for (int j=0; j< encrypt.length; j++) System.out.println(encrypt[j]);

        ByteBuffer bufferRec = ByteBuffer.wrap(encrypt);

        byte[] taskIDBytes = RabbitMQUtils.readByteArray(bufferRec);

        byte[] dataBytes_ = RabbitMQUtils.readByteArray(bufferRec);
        String dataReturn = readString(dataBytes_);

        for (int i=0; i< taskIDBytes.length; i++) System.out.print(taskIDBytes[i]+", ");
        System.out.println();
        System.out.println(dataReturn);
        */



        long taskSentTimestamp;
        long responseReceivedTimestamp;
        byte[] data;
        byte[] dataGold;
        String results;
        String golds;
        byte[] taskID;
        byte[] expectedData;
        byte[] receivedData;
        byte[] dataWithID;
        byte[] goldWithID;


        EvaluationModuleTest eval = new EvaluationModuleTest();
        eval.init();

        taskSentTimestamp=0L;
        responseReceivedTimestamp = 10L;

        results = "<http:/connection1>, <http:/connection2>, <http:/connection3>" ;
        data = writeString(results);
        receivedData = writeByteArrays(new byte[][] {data});

        golds =   "<http:/connection1>, <http:/connection2> , <http:/connection4>" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{1,1} , new byte[][] { dataGold }, null);
;

        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);


        taskSentTimestamp=11L;
        responseReceivedTimestamp = 13L;

        results = "<http:/connection4>, <http:/connection5>, <http:/connection5>" ;
        data = writeString(results);

        receivedData = writeByteArrays(new byte[][] { data });


        golds =   "<http:/connection4>, <http:/connection5>" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{1,2}, new byte[][] {dataGold }, null);


        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);



        taskSentTimestamp=13L;
        responseReceivedTimestamp = 0L;

        results = "<http:/connection6>, <http:/connection7>" ;
        data = writeString(results);

        receivedData = writeByteArrays(new byte[][] { data });


        golds =   "<http:/connection6>, <http:/connection7>, <http:/connection8>" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{2,1}, new byte[][] {dataGold }, null);

        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);




        taskSentTimestamp=22L;
        responseReceivedTimestamp = 0L;

        results = "<http:/connection8>, <http:/connection1>, <http:/connection9>" ;
        data = writeString(results);

        receivedData = writeByteArrays(new byte[][] { data });

        golds =   "<http:/connection7>, <http:/connection8>, <http:/connection9>" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{2,2}, new byte[][] { dataGold }, null);


        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);




        taskSentTimestamp=1L;
        responseReceivedTimestamp = 0L;

        results = "110" ;
        data = writeString(results);
        receivedData = writeByteArrays( new byte[][] { data });

        golds =   "100" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{0,1}, new byte[][] { dataGold }, null);

        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);


        taskSentTimestamp=1L;
        responseReceivedTimestamp = 3L;

        results = "100" ;
        data = writeString(results);

        receivedData = writeByteArrays(new byte[][] { data });

        golds =   "110" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{0,2}, new byte[][] { dataGold }, null);

        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);


        taskSentTimestamp=1L;
        responseReceivedTimestamp = 3L;

        results = "2" ;
        data = writeString(results);

        receivedData = writeByteArrays(new byte[][] { data });

        golds =   "3" ;
        dataGold = writeString(golds);

        expectedData = writeByteArrays(new byte[]{0,3}, new byte[][] { dataGold }, null );

        eval.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);



        eval.summarizeEvaluation();
    }

}
