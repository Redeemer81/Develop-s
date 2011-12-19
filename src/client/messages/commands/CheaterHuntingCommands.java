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
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.messages.ServernoticeMapleClientMessageCallback;
import net.world.remote.CheaterData;
import tools.MaplePacketCreator;

public class CheaterHuntingCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		if (splitted[0].equals("!whosthere")) {
			MessageCallback callback = new ServernoticeMapleClientMessageCallback(c);
			StringBuilder builder = new StringBuilder("Players on Map: ");
			for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
				if (builder.length() > 150) { // wild guess :o
					builder.setLength(builder.length() - 2);
					callback.dropMessage(builder.toString());
					builder = new StringBuilder();
				}
				builder.append(MapleCharacter.makeMapleReadable(chr.getName()));
				builder.append(", ");
			}
			builder.setLength(builder.length() - 2);
			c.getSession().write(MaplePacketCreator.serverNotice(6, builder.toString()));
		} else if (splitted[0].equals("!cheaters")) {
			try {
				List<CheaterData> cheaters = c.getChannelServer().getWorldInterface().getCheaters(c.getWorld());
				for (int x = cheaters.size() - 1; x >= 0; x--) {
					CheaterData cheater = cheaters.get(x);
					mc.dropMessage(cheater.getInfo());
				}
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
		}
		
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("whosthere", "", "", 1),
			new CommandDefinition("cheaters", "", "", 1),
		};
	}

}
