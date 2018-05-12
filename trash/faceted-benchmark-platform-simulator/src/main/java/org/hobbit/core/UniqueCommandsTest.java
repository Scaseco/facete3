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
package org.hobbit.core;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;

public class UniqueCommandsTest {

    @Test
    public void testCommands() throws IllegalArgumentException, IllegalAccessException {
        String[] definedCommands = new String[256];
        Class<Commands> clazz = Commands.class;
        Field[] fields = clazz.getFields();
        int commandId;
        for (int i = 0; i < fields.length; ++i) {
            if (!fields[i].getName().equals("ID_TO_COMMAND_NAME_MAP")) {
                commandId = fields[i].getByte(null);
                Assert.assertNull("The command " + fields[i].getName() + " has the same ID as the command "
                        + definedCommands[commandId], definedCommands[commandId]);
                definedCommands[commandId] = fields[i].getName();
            }
        }
    }

    @Test
    public void testControllerApiCommands() throws IllegalArgumentException, IllegalAccessException {
        String[] definedCommands = new String[256];
        Class<FrontEndApiCommands> clazz = FrontEndApiCommands.class;
        Field[] fields = clazz.getFields();
        int commandId;
        for (int i = 0; i < fields.length; ++i) {
            commandId = fields[i].getByte(null);
            Assert.assertNull("The command " + fields[i].getName() + " has the same ID as the command "
                    + definedCommands[commandId], definedCommands[commandId]);
            definedCommands[commandId] = fields[i].getName();
        }
    }
}
