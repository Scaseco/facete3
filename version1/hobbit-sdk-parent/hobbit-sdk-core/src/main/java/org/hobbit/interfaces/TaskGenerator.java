package org.hobbit.interfaces;

import java.io.IOException;

import org.hobbit.core.component.BaseComponent;

public interface TaskGenerator
    extends BaseComponent
{
    void generateTask(byte[] data) throws Exception;
    void sendTaskToSystemAdapter(String taskIdString, byte[] data) throws IOException;

}
