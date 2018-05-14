package org.hobbit.benchmark.faceted_browsing.config;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jena.shared.NotFoundException;

public class HealthcheckUtils {

	public static URL createUrl(String str) {
    	URL url;
		try {
			url = new URL(str);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return url;
	}
	
	
	public static void checkUrl(URL url) {
		try {
	        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			try {
		        connection.setRequestMethod("GET");
		        connection.connect();
		        int code = connection.getResponseCode();
		        if(code != 200) {
		        	//logger.info("Health check status: fail");
		        	throw new NotFoundException(url.toString());
		        }
			} finally {
				connection.disconnect();
			}
	    	//logger.info("Health check status: success");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
