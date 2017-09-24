package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.hobbit.interfaces.BaseComponent;
import org.hobbit.transfer.Publisher;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentBase
    implements BaseComponent
{
    private static final Logger logger = LoggerFactory.getLogger(ComponentBase.class);

    // The actual publisher of the command channel
    // All commands are re-publishehd via the local command publisher
    @Resource(name="commandPub")
    protected Publisher<ByteBuffer> remoteCommandPublisher;


    // This is the *local* publisher for the receiveCommand method
    // It combines data sent via the remote publisher and the receiveCommand function
    protected PublishingWritableByteChannelSimple commandPublisher = new PublishingWritableByteChannelSimple();



    // FIXME Should we rename to init() ? If so, we must ensure that subclasses' init() methods call super.init()
    @PostConstruct
    public void coreInit() {
        remoteCommandPublisher.subscribe(t -> {
            try {
                commandPublisher.write(t);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        logger.info("Seen command: " + command + " with " + data.length + " bytes");
        ByteBuffer bb = ByteBuffer.wrap(new byte[1 + data.length]).put(command).put(data);
        bb.rewind();

        try {
            commandPublisher.write(bb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
