/**
 * This file is part of core.
 *
 * core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.core.components.dummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Ignore;

@Ignore
public class DummySystemReceiver extends AbstractSystemAdapter {

    private final List<String> receivedData = Collections.synchronizedList(new ArrayList<String>());
    private final List<String> receivedTasks = Collections.synchronizedList(new ArrayList<String>());

    @Override
    public void receiveGeneratedData(byte[] data) {
        receivedData.add(RabbitMQUtils.readString(data));
    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        receivedTasks.add(taskId + RabbitMQUtils.readString(data));
    }

    /**
     * @return the received data
     */
    public List<String> getReceiveddata() {
        return receivedData;
    }

    /**
     * @return the received tasks
     */
    public List<String> getReceivedtasks() {
        return receivedTasks;
    }

}
