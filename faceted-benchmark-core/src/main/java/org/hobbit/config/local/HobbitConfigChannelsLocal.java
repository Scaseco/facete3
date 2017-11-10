package org.hobbit.config.local;

import org.hobbit.transfer.PublishingWritableByteChannel;
import org.hobbit.transfer.PublishingWritableByteChannelQueued;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;

public class HobbitConfigChannelsLocal {
    @Bean
    public Gson gson() {
        return new Gson();
        // return new GsonBuilder().setPrettyPrinting().create();
    }

    /*
     * Standard channels
     */

    @Bean(name = { "commandChannel", "commandPub" })
    public PublishingWritableByteChannel commandChannel() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "dataChannel", "dataPublisher" })
    public PublishingWritableByteChannel dataChannel() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "dg2tg", "dg2tgPub" })
    public PublishingWritableByteChannel dg2tg() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "dg2sa", "dg2saPub" })
    public PublishingWritableByteChannel dg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "tg2sa", "tg2saPub" })
    public PublishingWritableByteChannel tg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "tg2es", "tg2esPub" })
    public PublishingWritableByteChannel tg2es() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "sa2es", "sa2esPub" })
    public PublishingWritableByteChannel sa2es() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "es2em", "es2emPub" })
    public PublishingWritableByteChannel es2em() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "em2es", "em2esPub" })
    public PublishingWritableByteChannel em2es() {
        return new PublishingWritableByteChannelSimple();
        //return new PublishingWritableByteChannelQueued();
    }

}
