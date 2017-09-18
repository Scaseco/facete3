package org.hobbit.interfaces;

import java.io.IOException;

public interface TaskGenerator
    extends BaseComponent
{
    void generateTask(byte[] data) throws Exception;
    void sendTaskToSystemAdapter(String taskIdString, byte[] data) throws IOException;

}
