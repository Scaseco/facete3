package org.hobbit.benchmark.faceted_browsing.v1.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.utils.SupplierExtendedIteratorTriples;
import org.aksw.jena_sparql_api.ext.virtuoso.HealthcheckRunner;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.core.Constants;
import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceBuilder;
import org.hobbit.core.service.docker.impl.core.DockerServiceBuilderFactory;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.service.podigg.PodiggWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;

public class ConfigDataGeneratorFacetedBrowsing {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigDataGeneratorFacetedBrowsing.class);

	
	
		public static <T> Stream<T> stream(ExtendedIterator<T> it) {
			Stream<T> result = Streams.stream(it);
			result.onClose(() -> it.close());
			return result;
		}
		
	    public static Stream<Triple> createTripleStream(String fileNameOrUrl, Lang langHint) {
	    	ExtendedIterator<Triple> it = SupplierExtendedIteratorTriples.createTripleIterator(fileNameOrUrl, langHint);
	    	Stream<Triple> result = stream(it);
	    	
	    	return result;
	    }
	    
	    public static Stream<Triple> createPodiggDatasetViaDocker(
	    		DockerServiceBuilderFactory<?> dockerServiceBuilderFactory,
	    		String imageName,
	    		Map<String, String> env) {
			DockerServiceBuilder<?> dockerServiceBuilder = dockerServiceBuilderFactory.get();
			DockerService podiggService = dockerServiceBuilder
				.setImageName(imageName)					
				.setLocalEnvironment(env)
				.get();

	    	podiggService.startAsync().awaitRunning();
	    	
	    	String host = podiggService.getContainerId();
	    	
	    	//File targetFile = new File("/tmp/podigg");
	    	String str = "http://" + host + "/podigg/latest/lc.ttl";
	    	
	    	
	    	URL url;
			try {
				url = new URL(str);
			} catch (MalformedURLException e1) {
				throw new RuntimeException(e1);
			}
	    	
			// TODO Ensure the health check can be interrupted on service stop 
	    	new HealthcheckRunner(60 * 15, 1, TimeUnit.SECONDS, () -> {
	    		try {
			        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		            connection.setRequestMethod("GET");
		            connection.connect();
		            int code = connection.getResponseCode();
		            if(code != 200) {
		            	logger.info("Health check status: fail");
		            	throw new NotFoundException(url.toString());
		            }
		            connection.disconnect();
	            	logger.info("Health check status: success");
	    		} catch(Exception e) {
	    			throw new RuntimeException(e);
	    		}
		    	}).run();
	    	
	    	
//	    	try {
//				Desktop.getDesktop().browse(new URI("http://" + host + "/podigg/latst"));
//			} catch (IOException | URISyntaxException e1) {
//				throw new RuntimeException(e1);
//			}

	    	//ByteStreams.copy(new URL(url).openStream(), new FileOutputStream(targetFile));
	    	
	    	Stream<Triple> r = createTripleStream(url.toString(), null);
	    	r.onClose(() -> {
		    	try {
					podiggService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					throw new RuntimeException();
				}
	    	});
	    	
	    	return r;	    	
	    }
	    
//	    @Bean
//	    public TripleStreamSupplier dataGenerationMethod() {
//	        logger.info("*** DG: USING STATIC TEST DATASET - DO NOT USE FOR PRODUCTION ***");
//	    	return () -> {
//	    	    //String staticFile = "podigg-lc-small.ttl";
//	    	    //String staticFile = "podigg-lc-large.ttl";
//                //String staticFile = "podigg-lc-medium.ttl";
//		    	ExtendedIterator<Triple> it = RDFDataMgr.loadModel(staticFile).getGraph().find();
//	    		return Streams.stream(it).onClose(it::close);
//	    	};
//	    }
//	    
	    @Bean
	    public TripleStreamSupplier dataGenerationMethod(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory, @Value("${" + Constants.BENCHMARK_PARAMETERS_MODEL_KEY + ":{}}") String paramModelStr) {
	        
	        logger.info("DG: Supplied param model is: " + paramModelStr);
	        
	        // Load the benchmark.ttl config as it contains the parameter mapping
	        Model paramModel = ModelFactory.createDefaultModel();
	        RDFDataMgr.read(paramModel, new ByteArrayInputStream(paramModelStr.getBytes()), Lang.JSONLD);

	        Model meta = RDFDataMgr.loadModel("faceted-browsing-benchmark-v1-benchmark.ttl");
	        
	        Model model = ModelFactory.createDefaultModel();
	        model.add(paramModel);
	        model.add(meta);

	        
	        // Check if the quickTestRun parameter is set to true - if so, use test settings
	        Property pQueryTestRun = ResourceFactory.createProperty("http://w3id.org/bench#paramQuickTestRun");
	        Property pPreconfig = ResourceFactory.createProperty("http://w3id.org/bench#paramPreconfig");

	        boolean isParamModelEmpty = paramModel.isEmpty();
	        boolean isQuickTestRunSet = model.listStatements(null, pQueryTestRun, (RDFNode)null).nextOptional().map(Statement::getBoolean).orElse(false);
	        
	        String preconfig = model.listStatements(null, pPreconfig, (RDFNode)null).nextOptional().map(Statement::getString).orElse("").trim(); 
	        
	        if(isParamModelEmpty) {
		        logger.warn("*** TEST RUN FLAG HAS BEEN AUTOMATICALLY SET BECAUSE NO BENCHMARK PARAMETERS WERE PROVIDED - MAKE SURE THAT THIS IS EXPECTED ***");
	        	preconfig = "test";
	        } else if(isQuickTestRunSet) {
		        logger.warn("*** TEST RUN FLAG WAS MANUALLY SET - ANY OTHER PROVIDED BENCHMARK PARAMETERS ARE IGNORED - MAKE SURE THAT THIS IS EXPECTED ***");
	        	preconfig = "test";
	        }	        
	        
	        Map<String, String> params = new HashMap<>();
	        
	        
	        if(!preconfig.isEmpty()) {
		        logger.warn("*** PROVIDED BENCHMARK PARAMETERS ARE IGNORED - LOADING PRECONFIG: " + preconfig + " ***");

	        	Map<String, Map<String, String>> preconfigs = new HashMap<>();
	        	preconfigs.put("test", ImmutableMap.<String, String>builder()
		                .put("GTFS_GEN_SEED", "111")
		                .build());
	        	                
// Podigg medium
//	           if(true) {
//              params = ImmutableMap.<String, String>builder()
//              .put("GTFS_GEN_SEED", "111")
//              .put("GTFS_GEN_REGION__SIZE_X", "2000")
//              .put("GTFS_GEN_REGION__SIZE_Y", "2000")
//              .put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
//              .put("GTFS_GEN_STOPS__STOPS", "3000")
//              .put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
//              .put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "200000")
//              .put("GTFS_GEN_ROUTES__ROUTES", "1500")
//              .put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
//              .put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
//              .put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1.3")
//              .put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "31536000000")
//              .put("GTFS_GEN_CONNECTIONS__TIME_INITIAL", "0")
//              .build();                               
//	           }
	            
// Podigg large (to verify)	            
//	        	preconfigs.put("mocha2018", ImmutableMap.<String, String>builder()
//	                .put("GTFS_GEN_SEED", "111")
//	                .put("GTFS_GEN_REGION__SIZE_X", "2000")
//	                .put("GTFS_GEN_REGION__SIZE_Y", "2000")
//	                .put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
//	                .put("GTFS_GEN_STOPS__STOPS", "4000")
//	                .put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
//	                .put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "500000")
//	                .put("GTFS_GEN_ROUTES__ROUTES", "4000")
//	                .put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
//	                .put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
//	                .put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1.3")
//	                .put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "977616000000")
//	                .build());
	        
	        	preconfigs.put("mocha2018", ImmutableMap.<String, String>builder()
		                .put("GTFS_GEN_SEED", "111")
		                .put("GTFS_GEN_REGION__SIZE_X", "2000")
		                .put("GTFS_GEN_REGION__SIZE_Y", "2000")
		                .put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
		                .put("GTFS_GEN_STOPS__STOPS", "3000")
		                .put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
		                .put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "230000")
		                .put("GTFS_GEN_ROUTES__ROUTES", "3000")
		                .put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
		                .put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
		                .put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1.3")
		                .put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "977616000000")
		                .build());
	        	
	        	
	        	//preconfigs.put("test", preconfigs.get("mocha2018"));
	        	
		        params = preconfigs.get(preconfig);
		        if(params == null) {
		        	throw new RuntimeException("No preconfiguration [" + preconfig + "] registered");
		        }
	        
	        } else {

		        Property pOption = ResourceFactory.createProperty("http://w3id.org/bench#podiggOption");
		        List<Resource> props = model.listSubjectsWithProperty(pOption).toList();
		        for(Resource p : props) {
		            String key = p.getProperty(pOption).getString();
	
		            Property pp = ResourceFactory.createProperty(p.getURI());
		            List<RDFNode> values = model.listObjectsOfProperty(pp).toList();
	
	                logger.info("DG: Values of " + pp + " " + values);
	
	                if(values.size() > 1) {
		                throw new RuntimeException("Too many values; at most one expected for " + pp + ": " + values);
		            }
	
	                if(!values.isEmpty()) {
	                    RDFNode o = values.get(0);
	                    String val = o.asNode().getLiteralLexicalForm();
	                    
	                    params.put(key, val);
	                }	            
		        }
		    }
	        
//	        logger.info("Podigg options: " + params);
//	        
//	        params = ImmutableMap.<String, String>builder()
//	                .put("GTFS_GEN_SEED", "111")
//	                .put("GTFS_GEN_REGION__SIZE_X", "2000")
//	                .put("GTFS_GEN_REGION__SIZE_Y", "2000")
//	                .put("GTFS_GEN_REGION__CELLS_PER_LATLON", "200")
//	                .put("GTFS_GEN_STOPS__STOPS", "3000")
//	                .put("GTFS_GEN_CONNECTIONS__DELAY_CHANCE", "0.02")
//	                .put("GTFS_GEN_CONNECTIONS__CONNECTIONS", "200000")
//	                .put("GTFS_GEN_ROUTES__ROUTES", "1000")
//	                .put("GTFS_GEN_ROUTES__MAX_ROUTE_LENGTH", "50")
//	                .put("GTFS_GEN_ROUTES__MIN_ROUTE_LENGTH", "10")
//	                .put("GTFS_GEN_CONNECTIONS__ROUTE_CHOICE_POWER", "1")
//	                .put("GTFS_GEN_CONNECTIONS__TIME_FINAL", "31536000000")
//	                .build();
//
//	        params = new HashMap<>();
	        
	        logger.info("DG: Configuring podigg with parameters: " + params);

	        
	    	//String imageName = "podigg";
	    	String imageName = "git.project-hobbit.eu:4567/cstadler/faceted-browsing-benchmark-releases/podigg-lc-via-web-server";
	    	Map<String, String> env = ImmutableMap.<String, String>builder().putAll(params).build();
	    	return () -> createPodiggDatasetViaDocker(dockerServiceBuilderFactory, imageName, env);
	    }
		
//	    @Bean
	// public TripleStreamSupplier dataGenerationMethodOld() {
	// return () -> {
	// try {
	// return PodiggWrapper.test();
	// } catch (IOException | InterruptedException e) {
	// throw new RuntimeException(e);
	// }
	// };
	// }

	}
