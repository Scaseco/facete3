package org.hobbit.benchmark.faceted_browsing.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.function.Supplier;

import org.aksw.commons.util.compress.MetaBZip2CompressorInputStream;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark.BenchmarkLauncher;
import org.hobbit.benchmark.faceted_browsing.config.ConfigCommunicationWrapper;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceFactory;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceManagerClient;
import org.hobbit.benchmark.faceted_browsing.config.ConfigDockerServiceManagerServer;
import org.hobbit.benchmark.faceted_browsing.config.HobbitSdkConstants;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigCommandChannel;
import org.hobbit.benchmark.faceted_browsing.config.amqp.ConfigRabbitMqConnection;
import org.hobbit.core.Constants;
import org.hobbit.core.config.ConfigGson;
import org.hobbit.core.config.ConfigRabbitMqConnectionFactory;
import org.hobbit.core.config.RabbitMqFlows;
import org.hobbit.qpid.v7.config.ConfigQpidBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.common.collect.ImmutableMap;



//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class HobbitBenchmarkUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(HobbitBenchmarkUtils.class);

//	@Configuration
//	@TestPropertySource(properties = {"hostMode=true"})
//	public static class Context {
//	}
	
	
	public static InputStream openBz2InputStream(String name) throws IOException {
		InputStream rawIn = HobbitBenchmarkUtils.class.getClassLoader().getResourceAsStream(name);
		if(rawIn == null) {
			throw new IOException("Resource not found: " + name);
		}
		
		InputStream result = new MetaBZip2CompressorInputStream(rawIn);
		return result;
	}

	
	/**
	 * A different context layout, where the infrastructure (qpid server and docker service manager) are separated
	 * from the benchmark (controller) launcher
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	//@Test
	public static void testBenchmarkTwoAppContexts(String bcImageName, String saImageName, Class<?> dockerServiceFactoryOverridesClass) throws MalformedURLException, IOException {		
		
		
		//System.out.println(CharStreams.toString(new InputStreamReader(new URL("docker+http://foobar:8892/sparql").openStream(), StandardCharsets.UTF_8)));		
		//System.exit(0);
		
		String sessionId = RabbitMqFlows.idGenerator.get();

		Supplier<SpringApplicationBuilder> builderFactory = () ->
			new SpringApplicationBuilder()
			// Add the amqp broker
			.properties(new ImmutableMap.Builder<String, Object>()
					.put(HobbitSdkConstants.HOSTMODE_KEY, true)
					.put(HobbitSdkConstants.BC_IMAGE_NAME_KEY, bcImageName)
					.put(HobbitSdkConstants.SA_IMAGE_NAME_KEY, saImageName)
					.put(Constants.HOBBIT_SESSION_ID_KEY, "testsession" + "." + sessionId)
					//.put(ConfigRabbitMqConnectionFactory.AMQP_VHOST, "default")
					.build());
		
		SpringApplicationBuilder infrastructureBuilder = builderFactory.get()
			.sources(ConfigQpidBroker.class)
			// Register the docker service manager server component; for this purpose:
			// (1) Register any pseudo docker images - i.e. launchers of local components
			// (2) Configure a docker service factory - which creates service instances that can be launched
			// (3) configure the docker service manager server component which listens on the amqp infrastructure
			.child(
					ConfigGson.class,
					ConfigRabbitMqConnectionFactory.class,
					ConfigRabbitMqConnection.class,
					ConfigCommunicationWrapper.class,
					ConfigCommandChannel.class,
					dockerServiceFactoryOverridesClass,
					ConfigDockerServiceFactory.class,
					ConfigDockerServiceManagerServer.class)
			;

			
		SpringApplicationBuilder launcherBuilder = builderFactory.get()
				.sources(
						ConfigGson.class,
						ConfigRabbitMqConnectionFactory.class,
						ConfigRabbitMqConnection.class,
						ConfigCommunicationWrapper.class,
						ConfigCommandChannel.class,
						ConfigDockerServiceManagerClient.class,
						BenchmarkLauncher.class);

		try(ConfigurableApplicationContext envCtx = infrastructureBuilder.run()) {
			try(ConfigurableApplicationContext launcherCtx = launcherBuilder.run()) {
				logger.info("LAUNCHER CLOSING");
			} finally {
				logger.info("LAUNCHER CLOSED");
				logger.info("INFRA CLOSING");
			}
		} finally {
			logger.info("INFRA CLOSED");
		}
	}
	
	//@Test
	public void testBenchmarkCompact() throws MalformedURLException, IOException {		
		
		//System.out.println(CharStreams.toString(new InputStreamReader(new URL("docker+http://foobar:8892/sparql").openStream(), StandardCharsets.UTF_8)));		
		//System.exit(0);
		SpringApplicationBuilder builder = new SpringApplicationBuilder()
			// Add the amqp broker
			.properties(new ImmutableMap.Builder<String, Object>()
					.put("hostMode", true)
					.put(Constants.HOBBIT_SESSION_ID_KEY, "testsession" + "." + RabbitMqFlows.idGenerator.get())
					//.put(ConfigRabbitMqConnectionFactory.AMQP_VHOST, "default")
					.build())
			.sources(ConfigQpidBroker.class)
			// Register the docker service manager server component; for this purpose:
			// (1) Register any pseudo docker images - i.e. launchers of local components
			// (2) Configure a docker service factory - which creates service instances that can be launched
			// (3) configure the docker service manager server component which listens on the amqp infrastructure
			.child(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommunicationWrapper.class, ConfigCommandChannel.class, ConfigDockerServiceFactory.class, ConfigDockerServiceManagerServer.class)
			.child(ConfigGson.class, ConfigRabbitMqConnectionFactory.class, ConfigRabbitMqConnection.class, ConfigCommunicationWrapper.class, ConfigCommandChannel.class, ConfigDockerServiceManagerClient.class, BenchmarkLauncher.class);
			;

		try(ConfigurableApplicationContext ctx = builder.run()) {}

		
//		.child(ConfigRabbitMqConnectionFactory.class)
//		// Connect the docker service factory to the amqp infrastructure 
//		.child(ConfigDockerServiceFactoryHobbitFacetedBenchmarkLocal.class, ConfigDockerServiceManagerServiceComponent.class) // Connect the local docker service factory to the rabbit mq channels
//		// Add the benchmark component
//		.sibling(ConfigBenchmarkControllerChannels.class, ConfigDockerServiceManagerClientComponent.class, ConfigHobbitFacetedBenchmarkController.class);

	}

	
	// Not needed anymore
	@Deprecated
	public static void initUrlResolver() {
		// In the local environment:
		// Set up a URL stream handler that maps docker names to localhost
        final String prefix = "docker+";
		URL.setURLStreamHandlerFactory(protocol -> protocol.startsWith(prefix) ? new URLStreamHandler() {
		    protected URLConnection openConnection(URL url) throws IOException {
		        URI uri;
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
		    	

		        String scheme = uri.getScheme();
		        String host = uri.getHost();

	        	scheme = scheme.substring(prefix.length());
	        	host = "localhost";

		        URL rewrite;
				try {
					rewrite = new URI(scheme, uri.getUserInfo(), host, uri.getPort(),
					           uri.getPath(), uri.getQuery(), uri.getFragment()).toURL();
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
		        
		        
		    	return rewrite.openConnection();
		    }
		} : null);
	}

}
