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

import java.awt.Point;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleBuffStat;
import client.anticheat.CheatingOffense;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import server.MaplePortal;
import server.TimerManager;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;
import server.quest.MapleQuest;
import tools.HexTool;
import tools.Pair;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

public class DebugCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
		if (splitted[0].equals("!resetquest")) {
			MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
		} else if (splitted[0].equals("!nearestPortal")) {
			final MaplePortal portal = player.getMap().findClosestSpawnpoint(player.getPosition());
			mc.dropMessage(portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());
		} else if (splitted[0].equals("!spawndebug")) {
			c.getPlayer().getMap().spawnDebug(mc);
		} else if (splitted[0].equals("!timerdebug")) {
			TimerManager.getInstance().dropDebugInfo(mc);
                } else if (splitted[0].equals("!fakemount")) {
                    final int mount = Integer.parseInt(splitted[1]);
                    final int skill = Integer.parseInt(splitted[1]);
                    c.getSession().write(MaplePacketCreator.giveMount(mount, skill, Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.MONSTER_RIDING, 0))));
		} else if (splitted[0].equals("!threads")) {
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			String filter = "";
			if (splitted.length > 1) {
				filter = splitted[1];
			}
			for (int i = 0; i < threads.length; i++) {
				String tstring = threads[i].toString();
				if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
					mc.dropMessage(i + ": " + tstring);
				}
			}
		} else if (splitted[0].equals("!showtrace")) {
			if (splitted.length < 2) {
				throw new IllegalCommandSyntaxException(2);
			}
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			Thread t = threads[Integer.parseInt(splitted[1])];
			mc.dropMessage(t.toString() + ":");
			for (StackTraceElement elem : t.getStackTrace()) {
				mc.dropMessage(elem.toString());
			}
		} else if (splitted[0].equals("!fakerelog")) {
			c.getSession().write(MaplePacketCreator.getCharInfo(player));
			player.getMap().removePlayer(player);
			player.getMap().addPlayer(player);
		} else if (splitted[0].equals("!toggleoffense")) {
			try {
				CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
				co.setEnabled(!co.isEnabled());
			} catch (IllegalArgumentException iae) {
				mc.dropMessage("Offense " + splitted[1] + " not found");
			}
                } else if (splitted[0].equals("!sreactor")) {
            MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
            MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
            reactor.setDelay(-1);
            reactor.setPosition(c.getPlayer().getPosition());
            c.getPlayer().getMap().spawnReactor(reactor);

        } else if (splitted[0].equals("!hreactor")) {
            c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
        } else if (splitted[0].equals("!lreactor")) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
            for (MapleMapObject reactorL : reactors) {
                MapleReactor reactor2l = (MapleReactor) reactorL;
                //mc.dropMessage("Reactor: " + reactor2l.toString());
                mc.dropMessage("Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState());

            }
        } else if (splitted[0].equals("!dreactor")) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
            if (splitted[1].equals("all")) {
                for (MapleMapObject reactorL : reactors) {
                    MapleReactor reactor2l = (MapleReactor) reactorL;
                    // reactor2l.
                    //mc.dropMessage("Reactor: " + reactor2l.toString());
                    c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
                }
            } else {
                c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
            }
        } else if (splitted[0].equals("!rreactor")) {
          c.getPlayer().getMap().resetReactors();
        } else if (splitted[0].equals("!summontype")) {
          c.getPlayer().summontype = Integer.parseInt(splitted[1]);
	}
      }

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("resetquest", "", "", 3),
			new CommandDefinition("nearestPortal", "", "", 3),
			new CommandDefinition("spawndebug", "", "", 3),
			new CommandDefinition("timerdebug", "", "", 3),
			new CommandDefinition("threads", "", "", 3),
			new CommandDefinition("showtrace", "", "", 3),
			new CommandDefinition("toggleoffense", "", "", 3),
			new CommandDefinition("fakerelog", "", "", 1),
			new CommandDefinition("fakemount", "", "", 1),
			new CommandDefinition("summontype", "", "", 1),
                        new CommandDefinition("sreactor", "[id]", "Spawn a Reactor", 2),
                        new CommandDefinition("hreactor", "[object ID]", "Hit reactor", 2),
                        new CommandDefinition("rreactor", "", "Resets all reactors", 2),
                        new CommandDefinition("lreactor", "", "List reactors", 2),
                        new CommandDefinition("dreactor", "", "Remove a Reactor", 2),
		};
	}

}
