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

//modify by shaun166 to work on new command system pls don delete 

package client.messages.commands;

import client.messages.CommandDefinition;
import client.messages.Command;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.MapleClient;
import net.channel.ChannelServer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.StringUtil;
import net.world.remote.EventInfo;

public class eventsCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
        if (splitted[0].equals("!coke")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9500144);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(9500151);
                            MapleMonster mob2 = MapleLifeFactory.getMonster(9500152);
                            MapleMonster mob3 = MapleLifeFactory.getMonster(9500153);
                            MapleMonster mob4 = MapleLifeFactory.getMonster(9500154);
                            MapleMonster mob5 = MapleLifeFactory.getMonster(9500143);
                            MapleMonster mob6 = MapleLifeFactory.getMonster(9500145);
                            MapleMonster mob7 = MapleLifeFactory.getMonster(9500149);
                            MapleMonster mob8 = MapleLifeFactory.getMonster(9500147);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob2, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob3, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob4, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob5, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob6, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob7, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob8, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!papu")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8500001);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!nxslimes")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9400202);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        } else if (splitted[0].equals("!zakum")){
                            c.getPlayer().getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), c.getPlayer().getPosition());
                            for (int x = 8800003; x < 8800011; x++)
                                c.getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), c.getPlayer().getPosition());
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "The almighty Zakum has awakened!"));			
                        } else if (splitted[0].equals("!chaoszakum")){
                            c.getPlayer().getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800100), c.getPlayer().getPosition());
                            for (int x = 8800103; x < 8800111; x++)
                                c.getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), c.getPlayer().getPosition());
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "The almighty Zakum has awakened!"));
			} else if (splitted[0].equals("!ergoth")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9300028);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!ludimini")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8160000);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(8170000);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!cornian")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8150201);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(8150200);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!balrog")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8130100);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(8150000);
                            MapleMonster mob2 = MapleLifeFactory.getMonster(9400536);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob2, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!mushmom")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(6130101);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(6300005);
                            MapleMonster mob2 = MapleLifeFactory.getMonster(9400205);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob2, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!wyvern")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8150300);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(8150301);
                            MapleMonster mob2 = MapleLifeFactory.getMonster(8150302);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob2, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!pirate")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9300119);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(9300107);
                            MapleMonster mob2 = MapleLifeFactory.getMonster(9300105);
                            MapleMonster mob3 = MapleLifeFactory.getMonster(9300106);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob2, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob3, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!clone")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9001002);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(9001000);
                            MapleMonster mob2 = MapleLifeFactory.getMonster(9001003);
                            MapleMonster mob3 = MapleLifeFactory.getMonster(9001001);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob2, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob3, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!anego")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9400121);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!theboss")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9400300);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!snackbar")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9500179);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!papapixie")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9300039);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!horseman")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9400549);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!blackcrow")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9400014);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!leafreboss")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9400014);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(8180001);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!shark")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8150101);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(8150100);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!franken")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9300139);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(9300140);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!bird")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9300090);
                            MapleMonster mob1 = MapleLifeFactory.getMonster(9300089);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob1, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!pianus")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(8510000);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        }else if (splitted[0].equals("!centipede")){
                            MapleMonster mob0 = MapleLifeFactory.getMonster(9500177);
                            c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob0, c.getPlayer().getPosition());
                        } else if (splitted[0].equals("!horntail")){
                           c.getPlayer().getMap().spawnMonster(MapleLifeFactory.getMonster(8810026));
                           c.getPlayer().getMap().killMonster(8810026);
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(0, "The almighty Zakum has awakened!"));
                     } else if (splitted[0].equals("!setevent")) {
                            if(splitted.length < 2)
                            {
                                mc.dropMessage("Syntax Helper: !setevent <description>");
                            } else {
                                String description = StringUtil.joinStringFrom(splitted, 1);
                                EventInfo newEvent = new EventInfo(c.getPlayer().getName(), description, c.getPlayer().getMapId(), c.getChannel(), c.getWorld());
                                c.getChannelServer().getWorldInterface().setupEvent(newEvent);
                            }
                    } else if(splitted[0].equalsIgnoreCase("!endevent")) {
                        c.getChannelServer().getWorldInterface().endEvent();
                    }
        }
                    @Override
	            public CommandDefinition[] getDefinition() {
		    return new CommandDefinition[] {
			new CommandDefinition("coke", "", "", 1),
			new CommandDefinition("papu", "", "", 1),
                        new CommandDefinition("nxslimes", "", "", 1),
			new CommandDefinition("zakum", "", "", 1),
			new CommandDefinition("chaoszakum", "", "", 1),
			new CommandDefinition("ergoth", "", "", 1),
			new CommandDefinition("ludimini", "", "", 1),
			new CommandDefinition("cornian", "", "", 1),
			new CommandDefinition("balrog", "", "", 1),
			new CommandDefinition("mushmom", "", "", 1),

			new CommandDefinition("wyvern", "", "", 1),
			new CommandDefinition("pirate", "", "", 1),
                        new CommandDefinition("clone", "", "", 1),
			new CommandDefinition("anego", "", "", 1),
			new CommandDefinition("theboss", "", "", 1),
                        new CommandDefinition("snackbar", "", "", 1),
			new CommandDefinition("papapixie", "", "", 1),
                        new CommandDefinition("horseman", "", "", 1),
			new CommandDefinition("blackcrow", "", "", 1),
			new CommandDefinition("leafreboss", "", "", 1),
                        new CommandDefinition("shark", "", "", 1),
			new CommandDefinition("franken", "", "", 1),
                        new CommandDefinition("bird", "", "", 1),
			new CommandDefinition("pianus", "", "", 1),
			new CommandDefinition("centipede", "", "", 1),
                        new CommandDefinition("horntail", "", "", 1),
                        new CommandDefinition("setevent", "description", "Starts an event with the given description at the current map.", 1),
                        new CommandDefinition("endevent", "", "Ends an active event.", 1),
                        
		};
	}

}