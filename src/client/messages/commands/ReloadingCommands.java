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

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.CommandProcessor;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import net.ExternalCodeTableGetter;
import net.PacketProcessor;
import net.RecvPacketOpcode;
import net.SendPacketOpcode;
import net.channel.ChannelServer;
import scripting.portal.PortalScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleShopFactory;
import server.life.MapleMonsterInformationProvider;
import server.maps.MapleMap;
import client.MapleCharacter;

public class ReloadingCommands implements Command {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReloadingCommands.class);

	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
            IllegalCommandSyntaxException {
		ChannelServer cserv = c.getChannelServer();
                if (splitted[0].equalsIgnoreCase("!reloadops")) {
                    try {
                            ExternalCodeTableGetter.populateValues(SendPacketOpcode.getDefaultProperties(), SendPacketOpcode.values(), true);
                            ExternalCodeTableGetter.populateValues(RecvPacketOpcode.getDefaultProperties(), RecvPacketOpcode.values(), false);
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
                    PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
                    PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
                } else if (splitted[0].equalsIgnoreCase("!clearPortalScripts")) {
			PortalScriptManager.getInstance().clearScripts();
		} else if (splitted[0].equalsIgnoreCase("!clearmonsterdrops")) {
			MapleMonsterInformationProvider.getInstance().clearDrops();
		} else if (splitted[0].equalsIgnoreCase("!clearReactorDrops")) {
			ReactorScriptManager.getInstance().clearDrops();
		} else if (splitted[0].equalsIgnoreCase("!clearshops")) {
			MapleShopFactory.getInstance().clear();
		} else if (splitted[0].equalsIgnoreCase("!clearevents")) {
			for (ChannelServer instance : ChannelServer.getAllInstances()) {
				instance.reloadEvents();
			}
		} else if (splitted[0].equalsIgnoreCase("!reloadcommands")) {
			CommandProcessor.getInstance().reloadCommands();
		 }else if (splitted[0].equalsIgnoreCase("!reloadMap"))
                {
                       MapleMap oldMap = c.getPlayer().getMap();
                       MapleMap newMap = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId(), true, true, true, true, true);
                       for (MapleCharacter ch : oldMap.getCharacters())
                       {
                           ch.changeMap(newMap);
                       }
                       oldMap.empty();
                       c.getPlayer().getMap().respawn();
                }
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("reloadops", "", "", 3),
			new CommandDefinition("clearPortalScripts", "", "", 2),
			new CommandDefinition("clearmonsterdrops", "", "", 2),
			new CommandDefinition("clearReactorDrops", "", "", 2),
			new CommandDefinition("clearshops", "", "", 2),
			new CommandDefinition("clearevents", "", "", 2),
			new CommandDefinition("reloadcommands", "", "", 3),
			new CommandDefinition("reloadmap", "", "", 1),
		};
	}

}
