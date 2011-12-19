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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import server.life.MapleMonsterStats;
import java.io.File;
import java.util.LinkedList;
import java.rmi.RemoteException;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import net.MaplePacket;
import net.world.remote.CheaterData;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMap;
import net.world.remote.WorldLocation;
import java.net.InetAddress;
import server.MapleTrade;
import net.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.Pair;
import scripting.portal.PortalScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleShopFactory;
import server.life.MapleMonsterInformationProvider;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.ArrayList;
import tools.StringUtil;
import server.maps.HiredMerchant;

class JrGM {

    static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("ap")) {
            player.setRemainingAp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
        } else if (splitted[0].equals("chattype")) {
            player.toggleGMChat();
            player.message("You now chat in " + (player.getGMChat() ? "white." : "black."));
        } else if (splitted[0].equals("cleardrops")) {
            player.getMap().clearDrops(player, true);
        } else if (splitted[0].equals("dc")) {
            int level = 0;
            MapleCharacter victim;
            if (splitted[1].charAt(0) == '-') {
                    level = StringUtil.countCharacters(splitted[1], 'f');
                    victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
            } else {
                    victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            }
            victim.getClient().getSession().close();
            if (level >= 1) {
                    victim.getClient().disconnect();
            }
            if (level >= 2) {
                    victim.saveToDB(true);
                    cserv.removePlayer(victim);
            }

        } else if (splitted[0].equals("smegaoff")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setCanSmega(false);
            player.dropMessage("You have disabled " + victim.getName() + "'s megaphone privilages");
            if (!(c.getPlayer().getName().equals(victim.getName()))) {
                player.dropMessage("Your megaphone privilages have been disabled by a GM. If you continue to spam you will be temp. banned.");
            }
        } else if (splitted[0].equals("smegaon")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setCanSmega(true);
            player.dropMessage("You have enabled " + victim.getName() + "'s megaphone privilages");
            if (!(c.getPlayer().getName().equals(victim.getName()))) {
                player.dropMessage("Your megaphone privilages have been enabled by a GM. Please remember not to spam.");
            }
        } else if (splitted[0].equals("id")) {
            try {
                BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + splitted[1] + "&check=true").openConnection().getInputStream()));
                String s;
                while ((s = dis.readLine()) != null) {
                    player.dropMessage(s);
                }
                dis.close();
            } catch (Exception e) {
            }
        } else if (splitted[0].equals("job")) {
            player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
        } else if (splitted[0].equals("killall")) {
            List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                player.getMap().killMonster(monster, player, false);
                monster.giveExpToCharacter(player, (int) (monster.getExp() * c.getPlayer().getExpRate()), true, 1, 0);
            }
            player.dropMessage("Killed " + monsters.size() + " monsters.");
        } else if (splitted[0].equals("level")) {
            player.setLevel(Integer.parseInt(splitted[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, 0);
        } else if (splitted[0].equals("notice")) {
            int joinmod = 1;
            int range = -1;
            if (splitted[1].equals("m")) {
                range = 0;
            } else if (splitted[1].equals("c")) {
                range = 1;
            } else if (splitted[1].equals("w")) {
                range = 2;
            }

            int tfrom = 2;
            if (range == -1) {
                range = 2;
                tfrom = 1;
            }
            int type = getNoticeType(splitted[tfrom]);
            if (type == -1) {
                type = 0;
                joinmod = 0;
            }
            String prefix = "";
            if (splitted[tfrom].equals("nv")) {
                prefix = "[Notice] ";
            }
            joinmod += tfrom;
            MaplePacket packet = MaplePacketCreator.serverNotice(type, prefix +
                    joinStringFrom(splitted, joinmod));
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                try {
                    ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(
                            c.getPlayer().getName(), packet.getBytes());
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            }
            return true;
        } else if (splitted[0].equals("me")) {
            String prefix = "[" + c.getPlayer().getName() + "] ";
            String message = prefix + joinStringFrom(splitted, 1);
            c.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, message));
            return true;
        } else if (splitted[0].equals("whosthere")) {
            //	MessageCallback callback = new ServernoticeMapleClientMessageCallback(c);
            StringBuilder builder = new StringBuilder("Players on Map: ");
            for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
                if (builder.length() > 150) { // wild guess :o
                    builder.setLength(builder.length() - 2);
                    player.dropMessage(builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(MapleCharacter.makeMapleReadable(chr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            player.dropMessage(builder.toString());
            c.getSession().write(MaplePacketCreator.serverNotice(6, builder.toString()));
            return true;
        } else if (splitted[0].equals("cheaters")) {
            try {
                List<CheaterData> cheaters = c.getChannelServer().getWorldInterface().getCheaters(player.getWorld());
                for (int x = cheaters.size() - 1; x >= 0; x--) {
                    CheaterData cheater = cheaters.get(x);
                    player.dropMessage(cheater.getInfo());
                }
                if (cheaters.size() == 0) {
                    player.dropMessage("No cheaters! Hurrah!");
                }
            } catch (Exception e) {
                c.getChannelServer().reconnectWorld();
            }
            return true;
        } else if (splitted[0].equals("online")) {
            String playerStr = "";
            try {
                playerStr = cserv.getWorldInterface().getAllPlayerNames(player.getWorld());
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }
            int onlinePlayers = playerStr.split(", ").length;
            player.dropMessage("Online players: " + onlinePlayers);
            player.dropMessage(playerStr);
        } else if (splitted[0].equalsIgnoreCase("search")) {
            if (splitted.length > 2) {
                String search = joinStringFrom(splitted, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
                player.dropMessage("~Searching~ <<Type: " + splitted[1] + " | Search: " + search + ">>");
                if (!splitted[1].equalsIgnoreCase("ITEM")) {
                    if (splitted[1].equalsIgnoreCase("NPC")) {
                        data = dataProvider.getData("Npc.img");
                    } else if (splitted[1].equalsIgnoreCase("MAP")) {
                        data = dataProvider.getData("Map.img");
                    } else if (splitted[1].equalsIgnoreCase("MOB")) {
                        ArrayList<Pair<Integer, String>> searchRet = CommandProcessor.getMobsIDsFromName(search);
                        if(searchRet.isEmpty())
                             player.dropMessage("No mobs with the specified name were found.");
                        else
                        {
                            for(Pair<Integer, String> mobPair : searchRet)
                            {
                                player.dropMessage(mobPair.getLeft() + " - " + mobPair.getRight());
                            }
                        }
                    } else if (splitted[1].equalsIgnoreCase("SKILL")) {
                        data = dataProvider.getData("Skill.img");
                    } else {
                        player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
                    }
                    List<Pair<Integer, String>> searchList = new LinkedList<Pair<Integer, String>>();
                    for (MapleData searchData : data.getChildren()) {
                        int searchFromData = Integer.parseInt(searchData.getName());
                        String npcNameFromData = (splitted[1].equalsIgnoreCase("MAP") || splitted[1].equalsIgnoreCase("MAPS")) ? MapleDataTool.getString(searchData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(searchData.getChildByPath("mapName"), "NO-NAME") : MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                        searchList.add(new Pair<Integer, String>(searchFromData, npcNameFromData));
                    }
                    for (Pair<Integer, String> searched : searchList) {
                        if (searched.getRight().toLowerCase().contains(search.toLowerCase())) {
                            player.dropMessage(searched.getLeft() + " - " + searched.getRight());
                        }
                    }
                } else {
                    for(Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getItemDataByName(search))
                    {
                        player.dropMessage(itemPair.getRight() + " - " + itemPair.getLeft());
                    }
                    player.dropMessage("Search Complete.");
                }
            } else {
                player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
            }
        } else if (splitted[0].equals("ban")) {

            if (splitted.length < 3) {
                player.dropMessage("Syntax for !ban: !ban user reason");
                return false;
            }
            String originalReason = StringUtil.joinStringFrom(splitted, 2);
            String reason = c.getPlayer().getName() + " banned " + splitted[1] + ": " + originalReason;
            MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (target != null) {
                String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
                String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                reason += " (IP: " + ip + ")";
                target.ban(reason, true);
                player.dropMessage("Banned " + readableTargetName + " ipban for " + ip + " reason: " + originalReason);
            } else {
                if (MapleCharacter.ban(splitted[1], reason, false)) {
                    player.dropMessage("Offline Banned " + splitted[1]);
                } else {
                    player.dropMessage("Failed to ban " + splitted[1]);
                }
            }
            return true;
        } else if (splitted[0].equals("dc")) {
            int level = 0;
            MapleCharacter victim;
            if (splitted[1].charAt(0) == '-') {
                level = StringUtil.countCharacters(splitted[1], 'f');
                victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
            } else {
                victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            }
            
            if (level >= 1) {
                victim.getClient().disconnect();
            }
            if (level >= 2) {
                victim.saveToDB(true);
                cserv.removePlayer(victim);
            }
            victim.getClient().getSession().close();
            return true;

        } else if (splitted[0].equals("unban")) {
            try {
                PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1, norankupdate = 0 WHERE id = " + MapleCharacter.getIdByName(splitted[1]) + "");
                p.executeUpdate();
                p.close();
            } catch (Exception e) {
                player.message("Failed to unban " + splitted[1]);
                return true;
            }
            player.message("Unbanned " + splitted[1]);
        } else if (splitted[0].equals("map")) {
            c.getPlayer().changeMap(cserv.getMapFactory().getMap(Integer.parseInt(splitted[1])));
        } else if (splitted[0].equals("charinfo")) {
            StringBuilder builder = new StringBuilder();
            MapleCharacter other = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);

            builder.append(other.getName());
            builder.append(" at ");
            builder.append(other.getPosition().x);
            builder.append("/");
            builder.append(other.getPosition().y);
            builder.append(" ");
            builder.append(other.getHp());
            builder.append("/");
            builder.append(other.getCurrentMaxHp());
            builder.append("hp ");
            builder.append(other.getMp());
            builder.append("/");
            builder.append(other.getCurrentMaxMp());
            builder.append("mp ");
            builder.append(other.getExp());
            builder.append("exp hasParty: ");
            builder.append(other.getParty() != null);
            builder.append(" hasTrade: ");
            builder.append(other.getTrade() != null);
            builder.append(" remoteAddress: ");
            builder.append(other.getClient().getSession().getRemoteAddress());
            c.getPlayer().dropMessage(builder.toString());

        } else if (splitted[0].equals("warphere")) { //warps other char to u
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));

        } else if (splitted[0].equals("strike")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.addStrike(player.getName());
            victim.getClient().disconnect();
        } else if (splitted[0].equals("resetstrikes")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.resetStrikes();
        } else if (splitted[0].equals("warp")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (splitted.length == 2) {
                    MapleMap target = victim.getMap();
                    c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                } else {
                    int mapid = Integer.parseInt(splitted[2]);
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                    victim.changeMap(target, target.getPortal(0));
                }
            } else {
                try {
                    victim = c.getPlayer();
                    WorldLocation loc = c.getChannelServer().getWorldInterface().getLocation(splitted[1]);
                    if (loc != null) {
                        player.dropMessage("You will be cross-channel warped. This may take a few seconds.");
                        // WorldLocation loc = new WorldLocation(40000, 2);
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(loc.map);
                        String ip = c.getChannelServer().getIP(loc.channel);
                        c.getPlayer().getMap().removePlayer(c.getPlayer());
                        victim.setMap(target);
                        String[] socket = ip.split(":");
                        if (c.getPlayer().getTrade() != null) {
                            MapleTrade.cancelTrade(c.getPlayer());
                        }
                        try {
                            WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
                            wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
                        //wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
                        } catch (RemoteException e) {
                            c.getChannelServer().reconnectWorld();
                        }
                        c.getPlayer().saveToDB(true);
                        if (c.getPlayer().getCheatTracker() != null) {
                            c.getPlayer().getCheatTracker().dispose();
                        }
                        ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
                        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                            MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                            c.getSession().write(packet);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        int map = Integer.parseInt(splitted[1]);
                        MapleMap target = cserv.getMapFactory().getMap(map);
                        c.getPlayer().changeMap(target, target.getPortal(0));
                    }
                } catch (/* Remote */Exception e) {
                    player.dropMessage("Something went wrong " + e.getMessage());
                }
            }
        } else if (splitted[0].equals("spawn")) {
            int mid = Integer.parseInt(splitted[1]);
            int num = Math.min(StringUtil.getOptionalIntArg(splitted, 2, 1), 500);
            Integer hp = StringUtil.getNamedIntArg(splitted, 1, "hp");
            Integer exp = StringUtil.getNamedIntArg(splitted, 1, "exp");
            Double php = StringUtil.getNamedDoubleArg(splitted, 1, "php");
            Double pexp = StringUtil.getNamedDoubleArg(splitted, 1, "pexp");
            MapleMonster onemob = MapleLifeFactory.getMonster(mid);
            int newhp = 0;
            int newexp = 0;
            double oldExpRatio = ((double) onemob.getHp() / onemob.getExp());
            if (hp != null) {
                newhp = hp.intValue();
            } else if (php != null) {
                newhp = (int) (onemob.getMaxHp() * (php.doubleValue() / 100));
            } else {
                newhp = onemob.getMaxHp();
            }
            if (exp != null) {
                newexp = exp.intValue();
            } else if (pexp != null) {
                newexp = (int) (onemob.getExp() * (pexp.doubleValue() / 100));
            } else {
                newexp = onemob.getExp();
            }

            if (newhp < 1) {
                newhp = 1;
            }
/*            double newExpRatio = ((double) newhp / newexp);
            if (newExpRatio < oldExpRatio && newexp > 0) {
                player.dropMessage("The new hp/exp ratio is better than the old one. (" + newExpRatio + " < " +
                        oldExpRatio + ") Please don't do this");
                return false;
            }*/

            MapleMonsterStats overrideStats = new MapleMonsterStats();
            overrideStats.setHp(newhp);
            overrideStats.setExp(newexp);
            overrideStats.setMp(onemob.getMaxMp());

            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                mob.setHp(newhp);
                mob.setOverrideStats(overrideStats);
                c.getPlayer().getMap().spawnMonsterOnGroudBelow(mob, c.getPlayer().getPosition());

            }
            return true;
        } else if (splitted[0].equals("clearportalscripts")) {
            PortalScriptManager.getInstance().clearScripts();
        } else if (splitted[0].equals("clearmonsterdrops")) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
        } else if (splitted[0].equals("clearreactordrops")) {
            ReactorScriptManager.getInstance().clearDrops();
        } else if (splitted[0].equals("clearshops")) {
            MapleShopFactory.getInstance().clear();
        } else if (splitted[0].equals("clearevents")) {
            for (ChannelServer instance : ChannelServer.getAllInstances()) {
                instance.reloadEvents();
            }
        }else if (splitted[0].equalsIgnoreCase("reloadMap"))
        {
               MapleMap oldMap = c.getPlayer().getMap();
               MapleMap newMap = c.getChannelServer().getMapFactory().getMap(player.getMapId(), true, true, true, true, true);
               for (MapleCharacter ch : oldMap.getCharacters())
               {
                   ch.changeMap(newMap);
               }
               oldMap = null;
               c.getPlayer().getMap().respawn();
        }

        else if (splitted[0].equals("tempban")) {
			Calendar tempB = Calendar.getInstance();
			String originalReason = StringUtil.joinAfterString(splitted, ":");

			if (splitted.length < 4 || originalReason == null) {
				 player.dropMessage("Syntax helper: !tempban <name> [i / m / w / d / h] <amount> [r [reason id]] : Text Reason");
				//throw new IllegalCommandSyntaxException(4);
			}

			int yChange = StringUtil.getNamedIntArg(splitted, 1, "y", 0);
			int mChange = StringUtil.getNamedIntArg(splitted, 1, "m", 0);
			int wChange = StringUtil.getNamedIntArg(splitted, 1, "w", 0);
			int dChange = StringUtil.getNamedIntArg(splitted, 1, "d", 0);
			int hChange = StringUtil.getNamedIntArg(splitted, 1, "h", 0);
			int iChange = StringUtil.getNamedIntArg(splitted, 1, "i", 0);
			int gReason = StringUtil.getNamedIntArg(splitted, 1, "r", 7);

			String reason = c.getPlayer().getName() + " tempbanned " + splitted[1] + ": " + originalReason;

			if (gReason > 14) {
				player.dropMessage("You have entered an incorrect ban reason ID, please try again.");
				return true;
			}

			DateFormat df = DateFormat.getInstance();
			tempB.set(tempB.get(Calendar.YEAR) + yChange, tempB.get(Calendar.MONTH) + mChange, tempB.get(Calendar.DATE) +
				(wChange * 7) + dChange, tempB.get(Calendar.HOUR_OF_DAY) + hChange, tempB.get(Calendar.MINUTE) +
				iChange);

			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

			if (victim == null) {
				int accId = MapleClient.findAccIdForCharacterName(splitted[1]);
				if (accId >= 0 && MapleCharacter.tempban(reason, tempB, gReason, accId)) {
					player.dropMessage("The character " + splitted[1] + " has been successfully offline-tempbanned till " +
						df.format(tempB.getTime()) + ".");
				} else {
					player.dropMessage("There was a problem offline banning character " + splitted[1] + ".");
				}
			} else {
				victim.tempban(reason, tempB, gReason);
				player.dropMessage("The character " + splitted[1] + " has been successfully tempbanned till " +
					df.format(tempB.getTime()));
			}
		}

         else if (splitted[0].equals("say")) {
            try {
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, player.getName() + ": " + joinStringFrom(splitted, 1)).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
         }
         else if (splitted[0].equals("giftpoints")) {
             int delta = 0;
             if(splitted.length != 3)
             {
                 player.dropMessage("Syntax helper: !giftpoints <name> <amount>");
             }
             try
             {
                 delta = Integer.parseInt(splitted[2]);
             } catch (NumberFormatException nfe)
             {
                 player.dropMessage("Incorrect parameter - please ensure you abide to the syntax !giftpoints <name> <amount>");
             }
             MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if(victim != null)
                victim.setPoints(victim.getPoints() + delta);
            else
                player.dropMessage("Player " + splitted[1] + " not found.");
         }
         else if (splitted[0].equals("reconnect")) {
                cserv.reconnectWorld(true);
         }
         else if (splitted[0].equals("reconnectchan")) {
                ChannelServer.getInstance(Integer.parseInt(splitted[1])).reconnectWorld(true);
         } else if (splitted[0].equals("closeallmerchants")) {
           for(ChannelServer cserver : ChannelServer.getAllInstances())//TODO: implement into world interfaces
           {
               cserver.getHMRegistry().closeAndDeregisterAll();
           }
         } else if (splitted[0].equalsIgnoreCase("closemerchant"))
        {
             if(splitted.length != 2)
                 player.dropMessage("Syntax helper: !closemerchant <name>");
             HiredMerchant victimMerch = c.getChannelServer().getHMRegistry().getMerchantForPlayer(splitted[1]);
             if(victimMerch != null)
                 victimMerch.closeShop();
             else
                 player.dropMessage("The specified player is either not online or does not have a merchant.");
      //   }
        /* else if (splitted[0].equals("set")) {
             ArrayList<String> propNames;
            if(splitted.length != 3 || !(splitted[2].equalsIgnoreCase("on") || splitted[2].equalsIgnoreCase("off")))
            {
                c.getPlayer().dropMessage("Syntax helper: !set <property> on / off");
                return true;
            }
             else
            {
                try
                {
                    propNames = cserv.getWorldRegistry().getProperties().getPropertyNames();
                    if(propNames.contains(splitted[1]))
                    {
                        cserv.getWorldRegistry().getProperties().setProperty(splitted[1], Boolean.valueOf(splitted[2].equalsIgnoreCase("on")));
                        player.dropMessage("Property " + splitted[1] + " now changed to: " + splitted[2]);
                    } else {
                        player.dropMessage("Incorrect parameter. Current properties: ");
                        for(String s : propNames)
                            player.dropMessage(s);
                    }
                } catch (RemoteException re)
                {
                    cserv.reconnectWorld();
                }
             }
*/
         } else if (splitted[0].equals("warpmap")) {
             for (MapleCharacter chr : player.getMap().getCharacters())
             {
                 chr.changeMap(c.getChannelServer().getMapFactory().getMap(Integer.valueOf(splitted[1])));
             }
        } else {
            if (player.gmLevel() == 1) {
                player.message("GM Command " + heading + splitted[0] + " does not exist");
            }
            return false;
        }
        return true;
    }

    static String joinStringFrom(String arr[], int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    private static int getNoticeType(String typestring) {
        if (typestring.equals("n")) {
            return 0;
        } else if (typestring.equals("p")) {
            return 1;
        } else if (typestring.equals("l")) {
            return 2;
        } else if (typestring.equals("nv")) {
            return 5;
        } else if (typestring.equals("v")) {
            return 5;
        } else if (typestring.equals("b")) {
            return 6;
        }
        return -1;
    }
}
