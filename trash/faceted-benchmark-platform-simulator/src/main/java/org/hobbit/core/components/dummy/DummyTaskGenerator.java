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

import org.hobbit.core.components.AbstractTaskGenerator;
import org.junit.Ignore;

@Ignore
public class DummyTaskGenerator extends AbstractTaskGenerator {
    
    private final long taskProcessingTime;
    
    public DummyTaskGenerator() {
        this(0L);
    }

    public DummyTaskGenerator(long taskProcessingTime) {
        this.taskProcessingTime = taskProcessingTime;
    }

    @Override
    protected void generateTask(byte[] data) throws Exception {
        String taskId = getNextTaskId();
        Thread.sleep(taskProcessingTime);
        sendTaskToSystemAdapter(taskId, data);
    }

}
