package org.hobbit.core.component.spring.factory;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Resource;

import org.apache.jena.graph.Triple;
import org.hobbit.core.component.DataGeneratorComponentImpl;
import org.hobbit.core.component.DataPipe;
import org.hobbit.core.component.DataProcessorMocha;
import org.hobbit.core.component.DataSink;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.FactoryBean;

import com.google.common.util.concurrent.Service;

import io.reactivex.Flowable;

public class FactoryBeanDataProcessorMocha
	implements FactoryBean<Service>
{
    @Resource(name="dg2tgSender")
    protected Subscriber<ByteBuffer> toTaskGenerator;
 
    @Resource(name="dg2saSender")
    protected Subscriber<ByteBuffer> toSystemAdatper;

    @Resource(name="commandSender")
    protected Subscriber<ByteBuffer> commandSender;

    @Resource(name="dataSource")
	protected Supplier<Flowable<Triple>> dataSource;

    @Resource(name="dataSink")
	protected Consumer<Supplier<Flowable<Triple>>> dataSink;
    
    
//    @Resource(name="dataGenerationAction")
//    protected Runnable action;
    
    protected int batchSize;
    
	@Override
	public Service getObject() throws Exception {
		DataSink<Triple> dataSink = new DataProcessorMocha(toTaskGenerator, toSystemAdatper, commandSender, batchSize);
		DataPipe<Triple> dataPipe = new DataPipe<>(dataSource, dataSink);
		
		return new DataGeneratorComponentImpl<Triple>(
				commandSender,
				dataPipe);
	}

	@Override
	public Class<?> getObjectType() {
		return Service.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
