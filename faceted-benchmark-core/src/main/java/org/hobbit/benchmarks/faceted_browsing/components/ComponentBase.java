package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.hobbit.interfaces.BaseComponent;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentBase
    implements BaseComponent
{
    private static final Logger logger = LoggerFactory.getLogger(ComponentBase.class);


    // This is the *local* publisher for the receiveCommand method
    // In principle, this should become an injected resources, but right now the receive method is
    // the primary abstraction for receiving commands
    protected PublishingWritableByteChannelSimple commandPublisher = new PublishingWritableByteChannelSimple();

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
