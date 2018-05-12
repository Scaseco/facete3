package org.hobbit.core.component;

import java.io.Closeable;

import javax.annotation.PostConstruct;

public interface BaseComponent
    extends Closeable
//    extends Service
{
	//@PostConstruct
    void init() throws Exception;
    void receiveCommand(byte command, byte[] data);
}
