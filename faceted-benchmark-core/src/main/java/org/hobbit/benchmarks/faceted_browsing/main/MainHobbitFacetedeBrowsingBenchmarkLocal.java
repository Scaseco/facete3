package org.hobbit.benchmarks.faceted_browsing.main;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.hobbit.benchmarks.faceted_browsing.components.PseudoHobbitPlatformController;
import org.hobbit.benchmarks.faceted_browsing.components.ServiceManagerUtils;
import org.hobbit.config.local.ConfigHobbitLocalServices;
import org.hobbit.config.local.HobbitConfigChannelsLocal;
import org.hobbit.config.local.HobbitConfigLocalPlatformFacetedBenchmark;
import org.hobbit.core.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import com.google.common.util.concurrent.Service;

public class MainHobbitFacetedeBrowsingBenchmarkLocal {
	
	private static final Logger logger = LoggerFactory.getLogger(MainHobbitFacetedeBrowsingBenchmarkLocal.class);
	
    public static void main(String[] args) throws Exception {

    	//start();
    	
    	// The platform ensures that the system under test (sut) is
    	// ready before start up of the benchmark controller
    	// however, this may change in the future, such that the sut is
    	// only launched after all data and task generation preparation is complete.
    	
    	
    	// The system adapter is comprised of two components:
    	// The hobbit client which acts as a bridge between the task generator and the system under test
    	// The docker container that wraps both components
    	// Hence, either the container or the hobbit client can bring up the actual system under test.
    	
    	
        try(AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                HobbitConfigLocalPlatformFacetedBenchmark.class,
                HobbitConfigChannelsLocal.class,
                ConfigHobbitLocalServices.class)) {

        	
        	Service systemUnderTestService = null;
        	try {
        		systemUnderTestService = (Service)ctx.getBean("systemUnderTestService");
        		systemUnderTestService.startAsync();
        		systemUnderTestService.awaitRunning();
        		
// The system adapter has to send out the system_ready_signal
//        		WritableByteChannel commandChannel = (WritableByteChannel)ctx.getBean("commandChannel");
//        		commandChannel.write(ByteBuffer.wrap(new byte[] {Commands.SYSTEM_READY_SIGNAL}));
        		
        		
	        	Supplier<Service> systemAdapterServiceFactory = (Supplier<Service>)ctx.getBean("systemAdapterServiceFactory");
	        	Service systemAdapter = systemAdapterServiceFactory.get();
	        	systemAdapter.startAsync();
	        	systemAdapter.awaitRunning();
	        	
	        	
	            PseudoHobbitPlatformController commandHandler = ctx.getBean(PseudoHobbitPlatformController.class);
	            commandHandler.accept(ByteBuffer.wrap(new byte[] {Commands.START_BENCHMARK_SIGNAL}));
	            
	            // Sending the command blocks until the benchmark is complete
	            //System.out.println("sent start benchmark signal");
	            
        	} catch(Exception e) {
        		throw new RuntimeException(e);
        	} finally {
                if(systemUnderTestService != null) {
                    logger.debug("Shutting down system under test service");
                    systemUnderTestService.stopAsync();
                    ServiceManagerUtils.awaitTerminatedOrStopAfterTimeout(systemUnderTestService, 60, TimeUnit.SECONDS, 0, TimeUnit.SECONDS);
//                  systemUnderTestService.stopAsync();
//                  systemUnderTestService.awaitTerminated(60, TimeUnit.SECONDS);
                }        	
        	}
        }

    }
}
