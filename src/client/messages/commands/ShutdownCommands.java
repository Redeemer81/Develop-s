/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package client.messages.commands;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.CommandProcessor;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import server.ShutdownServer;

public class ShutdownCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																				IllegalCommandSyntaxException {
		if (splitted[0].equals("!shutdown")) {
			int time = 60000;
			if (splitted.length > 1) {
				time = Integer.parseInt(splitted[1]) * 60000;
			}
			CommandProcessor.forcePersisting();
			c.getChannelServer().shutdown(time);
		} else if (splitted[0].equals("!shutdownworld")) {
			int time = 60000;
			if (splitted.length > 1) {
				time = Integer.parseInt(splitted[1]) * 60000;
			}
			CommandProcessor.forcePersisting();
			c.getChannelServer().shutdownWorld(time);
			// shutdown
		} else if (splitted[0].equals("!shutdownnow")) {
			CommandProcessor.forcePersisting();
			new ShutdownServer(c.getChannel(), c.getWorld()).run();
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("shutdown", "[when in Minutes]", "Shuts down the current channel - don't use atm", 3),
			new CommandDefinition("shutdownnow", "", "Shuts down the current channel now", 3),
			new CommandDefinition("shutdownworld", "[when in Minutes]", "Cleanly shuts down all channels and the loginserver of this world", 3),
		};
	}

}
