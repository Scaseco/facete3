package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class FacetedBrowsingEncoders {
	
    public static String resultSetToJsonStr(ResultSet rs) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ResultSetFormatter.outputAsJSON(baos, rs); //resultSet);
    	//baos.flush();
    	String resultSetStr;
    	try {
    		resultSetStr = baos.toString(StandardCharsets.UTF_8.name());
    	} catch(UnsupportedEncodingException e) {
    		throw new RuntimeException(e);
    	}

    	return resultSetStr;
    }
    
    
}