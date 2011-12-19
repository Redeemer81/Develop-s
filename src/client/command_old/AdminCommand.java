/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package client.command_old;

import java.sql.PreparedStatement;
import client.MapleCharacter;
import client.MapleClient;
import java.util.Map.Entry;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import scripting.map.MapScriptManager;
import server.MapleOxQuiz;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.HexTool;
import tools.StringUtil;

class AdminCommand {
    static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equalsIgnoreCase("gc"))
            System.gc();
       
       
        else if (splitted[0].equalsIgnoreCase("npc")) {
            MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(splitted[1]));
            if (npc != null) {
                npc.setPosition(player.getPosition());
                npc.setCy(player.getPosition().y);
                npc.setRx0(player.getPosition().x + 50);
                npc.setRx1(player.getPosition().x - 50);
                npc.setFh(player.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            }
        } else if (splitted[0].equalsIgnoreCase("ox"))
            if (splitted[1].equalsIgnoreCase("on") && player.getMapId() == 109020001) {
                player.getMap().setOx(new MapleOxQuiz(player.getMap()));
                player.getMap().getOx().sendQuestion();
                player.getMap().setOxQuiz(true);
            } else {
                player.getMap().setOxQuiz(false);
                player.getMap().setOx(null);
            }

        else if (splitted[0].equalsIgnoreCase("pinkbean"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820001), player.getPosition());
        else if (splitted[0].equalsIgnoreCase("playernpc"))
            player.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]), Integer.parseInt(splitted[2]));
        else if (splitted[0].equalsIgnoreCase("reloadAllMaps"))
        {
            for (MapleMap map : c.getChannelServer().getMapFactory().getMaps().values())
            {
               MapleMap newMap = c.getChannelServer().getMapFactory().getMap(map.getId(), true, true, true, true, true);
               for (MapleCharacter ch : map.getCharacters())
               {
                   ch.changeMap(newMap);
               }
                newMap.respawn();
                map = null;
            }
        }

        else if (splitted[0].equalsIgnoreCase("setRates"))
        {
            double[] newRates = new double[4];
            if(splitted.length != 5)
            {
                player.dropMessage("!setrates syntax: <EXP> <DROP> <BOSSDROP> <MESO>. If field is unneeded, put -1 so for example for just an EXP rate change: !setrates 50 -1 -1 -1. Negative numbers multiply base EXP rate so for 2x EXP do !setrates -2 -1 -1 -1.");
                return true;
            } else
            {
                for(int i = 1; i < 5; i++)
                {
                    try
                    {
                        int rate = Integer.parseInt(splitted[i]);
                        newRates[i - 1] = rate;
                    } catch (NumberFormatException nfe)
                    {
                        player.dropMessage("There was an error with one of the arguments provided. Please only use numeric values.");
                        return true;
                    }
                }
            }
            try
            {
                c.getChannelServer().getWorldInterface().changeRates(newRates[0], newRates[1], newRates[2], newRates[3]);
            } catch(Exception e){
            c.getChannelServer().reconnectWorld();
            }
        }
        else if (splitted[0].equalsIgnoreCase("reloadmapspawns"))
            for (Entry<Integer, MapleMap> map : c.getChannelServer().getMapFactory().getMaps().entrySet())
                map.getValue().respawn();
        else if (splitted[0].equalsIgnoreCase("setgmlevel")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setGM(Integer.parseInt(splitted[2]));
            player.message("Done.");
            victim.getClient().disconnect();
        }   else if (splitted[0].equalsIgnoreCase("packet")) {
            if (!(splitted[1].equalsIgnoreCase("send") || splitted[1].equalsIgnoreCase("recv")))
            {
                player.dropMessage("Syntax helper: !packet <send/recv> <packet>");
                return true;
            }
            boolean send = splitted[1].equalsIgnoreCase("send");
            byte[] packet;
            try {
                packet = HexTool.getByteArrayFromHexString(StringUtil.joinStringFrom(splitted, 2));
            } catch (Exception e) {
                player.dropMessage("Invalid packet, please try again.");
                return true;
            }
            if(send)
                player.getClient().getSession().write(MaplePacketCreator.getRelayPacket(packet));
            else
                try{
                player.getClient().getSession().getHandler().messageReceived(player.getClient().getSession(), packet);
                } catch (Exception e){}
        } else if (splitted[0].equalsIgnoreCase("shutdown")) {
            int time = 60000;
            if (splitted.length > 1)
                time *= Integer.parseInt(splitted[1]);
            if (splitted[0].equalsIgnoreCase("shutdownnow"))
                time = 1;
            for (ChannelServer cs : ChannelServer.getAllInstances())
                cs.shutdown(time);
        } else if (splitted[0].equalsIgnoreCase("zakum")) {
            player.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
            for (int x = 8800003; x < 8800011; x++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), player.getPosition());
        } else if (splitted[0].equals("dcall")) {
                for(ChannelServer cserv : ChannelServer.getAllInstances())
                {
                    for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                        mch.getClient().getSession().close();
                        mch.getClient().disconnect();
                    }
                }
        }
        else
            return false;
        return true;
    }
 }

