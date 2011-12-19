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

import java.rmi.RemoteException;
import java.util.Map;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.messages.ServernoticeMapleClientMessageCallback;

public class ConnectedCommand implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception,
																					IllegalCommandSyntaxException {
		try {
			Map<Integer, Integer> connected = c.getChannelServer().getWorldInterface().getConnected();
			StringBuilder conStr = new StringBuilder("Connected Clients: ");
			boolean first = true;
			for (int i : connected.keySet()) {
				if (!first) {
					conStr.append(", ");
				} else {
					first = false;
				}
				if (i == 0) {
					conStr.append("Total: ");
					conStr.append(connected.get(i));
				} else {
					conStr.append("Channel");
					conStr.append(i);
					conStr.append(": ");
					conStr.append(connected.get(i));
				}
			}
			new ServernoticeMapleClientMessageCallback(c).dropMessage(conStr.toString());
		} catch (RemoteException e) {
			c.getChannelServer().reconnectWorld();
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("connected", "", "Shows how many players are connected on each channel", 1),
		};
	}

}
