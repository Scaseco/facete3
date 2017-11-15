package org.hobbit.transfer;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * TODO Replace WritableByteChannel with a Consumer<T>
 * @author raven Nov 6, 2017
 *
 */
public interface PublishingWritableByteChannel
    extends WritableByteChannel, Publisher<ByteBuffer>
{

}
