package org.hobbit.benchmark.faceted_browsing.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.hobbit.core.service.docker.api.DockerService;
import org.hobbit.core.service.docker.api.DockerServiceFactory;
import org.hobbit.core.service.docker.impl.core.DockerServiceFactoryChain;
import org.hobbit.core.service.docker.impl.docker_client.DockerServiceFactoryDockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public class ConfigDockerServiceFactory {
		
		private static final Logger logger = LoggerFactory.getLogger(ConfigDockerServiceFactory.class);

		
//      DockerServiceFactory<?> result = new DockerServiceFactorySpringApplicationBuilder(virtualDockerComponentRegistry);


		// TODO Inject the container image overrides
		public static DockerServiceFactory<?> createDockerServiceFactory(
				boolean hostMode,
				Map<String, String> env,
				DockerServiceFactory<?> dockerServiceFactoryOverrides) throws DockerCertificateException {
	        
	        // Configure the docker server component	        
	        DockerServiceFactory<?> core = DockerServiceFactoryDockerClient.create(hostMode, env, Collections.singleton("hobbit"));
	        

	        // FIXME Hostmode controls two aspects which should be separated: (1) use container IPs instead of names (2) override docker images with the component registry
	        if(hostMode) {        
	        	//DockerServiceFactory<?> localOverrides = ComponentUtils.createVirtualComponentDockerServiceFactory();
	        	core = new DockerServiceFactoryChain(dockerServiceFactoryOverrides, core);	        
	        }
	        
	        DockerServiceFactory<?> result = ComponentUtils.applyServiceWrappers(core);
	        
	        // Test starting a triple store and use it
	        if(false) {
	        	
		        DockerService service = result.create("tenforce/virtuoso", ImmutableMap.<String, String>builder().build());
		        service.startAsync().awaitRunning();
		        String name = service.getContainerId();	        
		        String url = "http://" + name + ":8890/sparql";
		        System.out.println("url: <" + url + ">");
		        
				try {
					System.out.println(CharStreams.toString(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
		        service.stopAsync().awaitTerminated();
				System.exit(0);
	

	        }
	        
	        if(false) {
		        DockerService x = result.create("git.project-hobbit.eu:4567/gkatsibras/facetedsystem/image", ImmutableMap.<String, String>builder().build());
		        x.startAsync().awaitRunning();
		        System.out.println("SERVICE IS RUNNING");
		        x.stopAsync().awaitTerminated();
		        System.out.println("SERVICE TERMINATED");
	        }
	        
	        return result;			
		}
		
		@Bean
		public DockerServiceFactory<?> dockerServiceFactory(
				@Value("${" + HobbitSdkConstants.HOSTMODE_KEY + ":false}") boolean hostMode,
				@Value("${HOBBIT_RABBIT_HOST:localhost}") String envStr,
				@Qualifier("dockerServiceFactoryOverrides") DockerServiceFactory<?> dockerServiceFactoryOverrides
				) throws DockerCertificateException {
			Map<String, String> env = new ImmutableMap.Builder<String, String>()
					.put("HOBBIT_RABBIT_HOST", envStr)
					.build();
			
			DockerServiceFactory<?> result = createDockerServiceFactory(hostMode, env, dockerServiceFactoryOverrides);
			return result;
		}
	}