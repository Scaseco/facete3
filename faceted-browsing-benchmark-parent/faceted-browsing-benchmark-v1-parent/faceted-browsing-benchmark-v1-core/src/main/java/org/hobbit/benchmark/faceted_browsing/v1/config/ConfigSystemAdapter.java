package org.hobbit.benchmark.faceted_browsing.v1.config;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import org.aksw.commons.service.core.BeanWrapperService;
import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.hobbit.benchmark.common.launcher.ConfigsFacetedBrowsingBenchmark;
import org.hobbit.benchmark.faceted_browsing.config.amqp.DataQueueFactory;
import org.hobbit.core.Constants;
import org.hobbit.core.service.docker.DockerServiceBuilderFactory;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.reactivex.Flowable;

// TODO Shouldn't the channel setup go to core?
public class ConfigSystemAdapter {

	    @Inject
	    protected DataQueueFactory dataQueueFactory;
	    
	    
		@Bean
		public Channel dg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}
		
	    @Bean
	    public Flowable<ByteBuffer> dg2saReceiver(@Qualifier("dg2saChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
	    }

        //SparqlQueryConnection queryConn = new SparqlQueryConnectionJsa(tmp.getQueryExecutionFactory());
        //SparqlUpdateConnection updateConn = new SparqlUpdateConnectionJsa(tmp.getUpdateExecutionFactory());
        //RDFDatasetConnection
        //RDFDatasetConnection datasetConn = new RDFDatasetConnectionVirtuoso(queryConn, sqlConn);
        
        //RDFConnection result = new RDFConnectionModular(queryConn, updateConn, null);

		
	    // Jena
//	    @Bean
//	    public BeanHolder<FusekiServer> fusekiServer() {
//			//Dataset ds = DatasetFactory.create();
//	    	Dataset ds = DatasetFactory.createTxnMem() ;
//			FusekiServer server = FusekiServer.create()
//			  .add("/ds", ds)
//			  .build();
//			
//			//server.start() ;
//			
//			return new BeanHolder<>(server, FusekiServer::start, FusekiServer::stop);
//	    }
//		@Bean
//		public RDFConnection systemUnderTestRdfConnection(BeanHolder<FusekiServer> fusekiServer) {
//			int port = fusekiServer.getBean().getPort();
//			String url = "http://localhost:" + port + "/ds";
//			return RDFConnectionFactory.connect(url);
//		}
		
		@Bean
		public RDFConnection systemUnderTestRdfConnection() {
			//SparqlService tmp = FluentSparqlService.forModel().create();
		    //RDFConnection result = new RDFConnectionLocal(DatasetFactory.create());
		    RDFConnection result = RDFConnectionFactory.connect(DatasetFactory.create());

			//RDFConnection result = RDFConnectionFactory.connect(DatasetFactory.create(ModelFactory.createDefaultModel()));
	        
			
			
			return result;
		}

//		@Bean
		public RDFConnection systemUnderTestRdfConnectionz(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
            SparqlBasedService service = ConfigsFacetedBrowsingBenchmark.createVirtuosoSparqlService("tenforce/virtuoso", dockerServiceBuilderFactory);

            service.startAsync().awaitRunning();
            
            return service.createDefaultConnection();
		}

		///v@Bean
		public BeanWrapperService<SparqlBasedService> systemService(DockerServiceBuilderFactory<?> dockerServiceBuilderFactory) {
			SparqlBasedService service = ConfigsFacetedBrowsingBenchmark.createVirtuosoSparqlService("tenforce/virtuoso", dockerServiceBuilderFactory);
		    return new BeanWrapperService<>(service);
		}
		
	    // Virtuoso
	    ///v@Bean
		public RDFConnection systemUnderTestRdfConnection(BeanWrapperService<SparqlBasedService> systemService) {
//		    	SparqlBasedService service = createVirtuosoSparqlService(dockerServiceBuilderFactory);
//		    	service.startAsync().awaitRunning();
//		    	return result;
//		    	
//	    	DockerService service = dockerClient.create("tenforce/virtuoso", null);
//
//			service.startAsync().awaitRunning();
//			String host = service.getContainerId();
//        	String url = "http://" + host + ":8890/";
		    RDFConnection result = systemService.getService().createDefaultConnection();
	        //RDFConnection result = RDFConnectionFactory.connect(url);

			
//	    	SparqlService tmp = FluentSparqlService.forModel().create();
//			RDFConnection result = new RDFConnectionLocal(DatasetFactory.create());
			
	        return result;
		}

		
		@Bean
		public Channel tg2saChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Flowable<ByteBuffer> tg2saReceiver(@Qualifier("tg2saChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createReceiver(channel, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
	    }

		@Bean
		public Channel sa2esChannel(Connection connection) throws IOException {
			return connection.createChannel();
		}

	    @Bean
	    public Subscriber<ByteBuffer> sa2esSender(@Qualifier("sa2esChannel") Channel channel) throws Exception {
	        return dataQueueFactory.createSender(channel, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
	    }
	}