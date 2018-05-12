package org.hobbit.core.config;

import java.nio.ByteBuffer;

import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public class HobbitConfigChannelsLocal {
    @Bean
    public Gson gson() {
        return new Gson();
        // return new GsonBuilder().setPrettyPrinting().create();
    }

    /*
     * Standard channels
     */

//    public static <T> Flowable<T> createDefaultPublishSubjectFlowable() {
//    	FlowableProcessor<T> result = PublishProcessor.create();
////    	PublishSubject<T> publishSubject = PublishSubject.create();
////    	publishSubject.onNext(t);
////    	Flowable<T> result = publishSubject.toFlowable(BackpressureStrategy.BUFFER);
//
//    	return result;
//    }
    
    @Bean(name = { "commandChannel", "commandPub" })
    public FlowableProcessor<ByteBuffer> commandChannel() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "dataChannel", "dataPublisher" })
    public FlowableProcessor<ByteBuffer> dataChannel() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "dg2tg", "dg2tgPub" })
    public FlowableProcessor<ByteBuffer> dg2tg() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "dg2sa", "dg2saPub" })
    public FlowableProcessor<ByteBuffer> dg2sa() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "tg2sa", "tg2saPub" })
    public FlowableProcessor<ByteBuffer> tg2sa() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "tg2es", "tg2esPub" })
    public FlowableProcessor<ByteBuffer> tg2es() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "sa2es", "sa2esPub" })
    public FlowableProcessor<ByteBuffer> sa2es() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "es2em", "es2emPub" })
    public FlowableProcessor<ByteBuffer> es2em() {
    	return PublishProcessor.create();
    }

    @Bean(name = { "em2es", "em2esPub" })
    public FlowableProcessor<ByteBuffer> em2es() {
    	return PublishProcessor.create();
    }

}

//@Bean(name = { "commandChannel", "commandPub" })
//public FlowableProcessor<ByteBuffer> commandChannel() {
//	return PublishProcessor.create();
//}
//
//@Bean(name = { "dataChannel", "dataPublisher" })
//public PublishingWritableByteChannel dataChannel() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "dg2tg", "dg2tgPub" })
//public PublishingWritableByteChannel dg2tg() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "dg2sa", "dg2saPub" })
//public PublishingWritableByteChannel dg2sa() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "tg2sa", "tg2saPub" })
//public PublishingWritableByteChannel tg2sa() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "tg2es", "tg2esPub" })
//public PublishingWritableByteChannel tg2es() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "sa2es", "sa2esPub" })
//public PublishingWritableByteChannel sa2es() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "es2em", "es2emPub" })
//public PublishingWritableByteChannel es2em() {
//    return new PublishingWritableByteChannelSimple();
//}
//
//@Bean(name = { "em2es", "em2esPub" })
//public PublishingWritableByteChannel em2es() {
//    return new PublishingWritableByteChannelSimple();
//    //return new PublishingWritableByteChannelQueued();
//}
