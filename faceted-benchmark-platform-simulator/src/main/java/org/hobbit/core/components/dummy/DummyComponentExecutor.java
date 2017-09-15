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

import org.apache.commons.io.IOUtils;
import org.hobbit.core.components.Component;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class DummyComponentExecutor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyComponentExecutor.class);

    protected Component component;
    protected boolean success;

    public DummyComponentExecutor(Component component) {
        this.component = component;
    }

    @Override
    public void run() {
        success = true;
        try {
            // initialize the component
            component.init();
            // run the component
            component.run();
        } catch (Throwable t) {
            LOGGER.error("Exception while executing component. Exiting with error code.", t);
            success = false;
        } finally {
            IOUtils.closeQuietly(component);
        }
    }

    /**
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

}
