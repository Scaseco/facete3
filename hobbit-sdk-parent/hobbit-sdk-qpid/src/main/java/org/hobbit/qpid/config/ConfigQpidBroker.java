package org.hobbit.qpid.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.inject.Inject;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@PropertySource("classpath:/local-config.properties")
public class ConfigQpidBroker {
	@Inject
	protected Environment env;
	
	/**
	 * If the resource is a file, returns the corresponding file object.
	 * Otherwise, creates a temporary file and copies the resource's data into it.
	 * 
	 * 
	 * @param resource
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
    public static File getResourceAsFile(Resource resource, String prefix, String suffix) throws IOException {
    	File result;
    	try {
    		result = resource.getFile();
    	} catch(Exception e) {
    	
	    	//FileCopyUtils.copy(in, out)
	    	result = File.createTempFile(prefix, suffix);
	    	Files.copy(resource.getInputStream(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    	result.deleteOnExit();
    	}
    	
    	return result;
    }
	@Bean(destroyMethod="shutdown")
	public Broker broker() throws Exception {
        String amqpInitialConfigUrl = ConfigQpidBroker.getResourceAsFile(new ClassPathResource("amqp-initial-config.json"), "amqp-config-", ".json").getAbsoluteFile().toURI().toURL().toString();

	    Broker broker = new Broker();
	    BrokerOptions brokerOptions = new BrokerOptions();

//	    brokerOptions.setConfigProperty('qpid.amqp_port',"${amqpPort}")
//	    brokerOptions.setConfigProperty('qpid.http_port', "${httpPort}")
//	    brokerOptions.setConfigProperty('qpid.home_dir', homePath);

	    brokerOptions.setInitialConfigurationLocation(amqpInitialConfigUrl); //"classpath:/amqp-initial-config.json");
	    brokerOptions.setConfigProperty("qpid.amqp_port", env.getProperty("spring.amqp.port"));
	    brokerOptions.setConfigProperty("qpid.broker.defaultPreferenceStoreAttributes", "{\"type\": \"Noop\"}");
	    brokerOptions.setConfigProperty("qpid.vhost", env.getProperty("spring.amqp.vhost"));
	    brokerOptions.setConfigurationStoreType("Memory");
	    brokerOptions.setStartupLoggedToSystemOut(false);
	    
	    broker.startup(brokerOptions);
	    //System.out.println("Broker starting...");
	    //Thread.sleep(5000);
	    return broker;
	}
}
