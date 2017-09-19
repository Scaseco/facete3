package org.hobbit.transfer;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public interface PublishingWritableByteChannel
    extends WritableByteChannel, Publisher<ByteBuffer>
{

}
