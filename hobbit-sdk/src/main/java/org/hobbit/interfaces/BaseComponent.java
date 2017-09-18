package org.hobbit.interfaces;


public interface BaseComponent {
    public void init() throws Exception;
    public void receiveCommand(byte command, byte[] data);
}
