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


import client.MapleCharacter;
import client.messages.CommandDefinition;
import client.messages.Command;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import server.MaplePortal;
import server.maps.MapleMap;
import client.MapleClient;
import net.channel.ChannelServer;


public class gotoCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
        if (splitted[0].equals("!goto")) {
                    //Map name array
                    int[] gotomapid = { 
                        180000000, //GmMap
                        60000, //Southperry
                        1010000, //Amherst
                        100000000, //henesys 
                        101000000, //ellinia 
                        102000000, //perion
                        103000000, //kerning
                        104000000, //lith
                        105040300, //sleepywood
                        110000000, //florina
                        200000000, //orbis
                        209000000, //happy
                        211000000, //elnath
                        220000000, //ludi
                        230000000, //aqua
                        240000000, //leafre
                        250000000, //mulung
                        251000000, //herb
                        221000000, //omega
                        222000000, //korean (Folk Town)
                        600000000, //nlc (New Leaf City)
                        990000000, //excavation (Sharenian/Excavation Site)
                        230040420, //Pianus cave
                        240060200, //Horned Tail's cave
                        100000005, //Mushmom
                        240020101, //Griffey
                        240020401, //Manon
                        682000001, //Headless Horseman
                        105090900, //Jr.Balrog
			280030000, //Zakum's Altar
			220080001, //Papulatus map
			801000000, //showa Town
			200000301, //Guild HeadQuarters
			800000000, //Shrine (Mushroom Shrine)
			910000000, //Free Market Entrance
			240040511, //Skelegon map (Leafre)
                        260000100, //ariant
                        540010000, //Singapore
                        610010004, //Crimsonwood
                        211040300, //Coke World
                    };
                    String[] gotomapname = { 
                        "gmmap",
                        "southperry",
                        "amherst",
                        "henesys", 
                        "ellinia", 
                        "perion", 
                        "kerning", 
                        "lith", 
                        "sleepywood", 
                        "florina",
                        "orbis", 
                        "happy", 
                        "elnath", 
                        "ludi", 
                        "aqua", 
                        "leafre", 
                        "mulung", 
                        "herb", 
                        "omega", 
                        "korean", 
                        "nlc",
                        "excavation",
                        "pianus",
                        "horntail",
                        "mushmom",
                        "griffey",
                        "manon",
                        "horseman",
                        "balrog",
			"zakum",
			"papu",
			"showa",
			"guild",
			"shrine",
			"fm",
			"skelegon",
                        "ariant",
                        "singapore",
			"crimson",
			"coke",
                    };
                    //Function
                    if (splitted.length < 2) { //If no arguments, list options.
                        mc.dropMessage("Syntax: !goto <mapname> <optional_target>, where target is char name and mapname is one of:");
                        mc.dropMessage("gmmap, southperry, amherst, henesys, ellinia, perion, kerning, lith, sleepywood, florina,");
                        mc.dropMessage("orbis, happy, elnath, ludi, aqua, leafre, mulung, herb, omega, korean, nlc, excavation, pianus");
                        mc.dropMessage("horntail, mushmom, griffey, manon, horseman, balrog, zakum, papu, showa, guild, shrine, fm, skelegon, ariant, crmisonwood, coke");
                    } else {
                        for (int i = 0; gotomapid[i] != 0 && gotomapname[i] != null; ++i) { //for every array which isn't empty
  if (splitted[1].equals(gotomapname[i])) { //If argument equals name
      ChannelServer cserv = c.getChannelServer();
    MapleMap target = cserv.getMapFactory().getMap(gotomapid[i]);
    MaplePortal targetPortal = target.getPortal(0);
    if (splitted.length < 3) { //If no target name, continue
      MapleCharacter player = c.getPlayer();
      player.changeMap(target, targetPortal);
    } else if (splitted.length > 2) { //If target name, new target
      MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
      victim.changeMap(target, targetPortal);
                }
  }
                        }     
                    }

        }
}
	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("goto", "?", "go <town/map name>", 1),
			
		};
	}

        }
        