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

import static client.messages.CommandProcessor.getOptionalIntArg;
import client.MapleClient;
import client.MapleCharacter;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.HexTool;

public class TestCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,													IllegalCommandSyntaxException {
            MapleCharacter player = c.getPlayer();
            if (splitted[0].equals("!clock")) {
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getClock(getOptionalIntArg(splitted, 1, 60)));
            }   else if (splitted[0].equalsIgnoreCase("!packet")) {
                if (!(splitted[1].equalsIgnoreCase("send") || splitted[1].equalsIgnoreCase("recv")))
                    {
                        player.dropMessage("Syntax helper: !packet <send/recv> <packet>");
                        return;
                    }
                    boolean send = splitted[1].equalsIgnoreCase("send");
                    byte[] packet;
                    try {
                        packet = HexTool.getByteArrayFromHexString(StringUtil.joinStringFrom(splitted, 2));
                    } catch (Exception e) {
                        player.dropMessage("Invalid packet, please try again.");
                        return;
                    }
                    if(send)
                        player.getClient().getSession().write(MaplePacketCreator.getRelayPacket(packet));
                    else
                        try{
                        player.getClient().getSession().getHandler().messageReceived(c.getSession(), packet);
                        } catch (Exception e){}
                }

	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("packet", "<send / recv> <packet hex string>", "emulates sending a packet FROM the server or allows the server to emulate receiving a packet FROM the user", 3),
			new CommandDefinition("clock", "[time]", "Shows a clock to everyone in the map", 1),
		};
	}

}
