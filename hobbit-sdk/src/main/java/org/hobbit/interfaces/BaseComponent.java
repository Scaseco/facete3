package org.hobbit.interfaces;

import java.io.Closeable;

public interface BaseComponent
    extends Closeable
//    extends Service
{
    void init() throws Exception;
    void receiveCommand(byte command, byte[] data);
}
