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
import client.IItem;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import constants.ExpTable;
import java.awt.Point;
import java.io.File;
import tools.DatabaseConnection;
import tools.StringUtil;
import net.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import tools.MaplePacketCreator;
import scripting.portal.PortalScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleShopFactory;
import server.life.MapleMonsterInformationProvider;
import net.ExternalCodeTableGetter;
import net.PacketProcessor;
import net.SendPacketOpcode;
import net.RecvPacketOpcode;
import client.Item;

class GMCommand {
    static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("ap")) {
            player.setRemainingAp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
        } else if(splitted[0].equalsIgnoreCase("clock")) {
            player.getMap().setClock(true);
            player.getMap().setTimeLimit(Integer.parseInt(splitted[1]));
        } else if(splitted[0].equalsIgnoreCase("clockd")) player.getMap().setClock(false);
         else if (splitted[0].equals("buffme")) {
            final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i : array)
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
        } else if (splitted[0].equals("chattype")) {
            player.toggleGMChat();
            player.message("You now chat in " + (player.getGMChat() ? "white." : "black."));
        } else if (splitted[0].equals("cody"))
            NPCScriptManager.getInstance().start(c, 9200000, null, null);
        else if (splitted[0].equals("dispoNPCse")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.message("Done.");
        } else if (splitted[0].equals("mynpcpos")) {
		    Point pos = c.getPlayer().getPosition();
		    player.message("CY: " + pos.y + " | RX0: " + (pos.x + 50) + " | R: "+ pos.x +" | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getMap().getFootholds().findBelow(pos).getId());
		}else if (splitted[0].equals("fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setFame(Integer.parseInt(splitted[2]));
            victim.updateSingleStat(MapleStat.FAME, victim.getFame());
        } else if (splitted[0].equals("giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(1, Integer.parseInt(splitted[2]));
            player.message("Done");
        } else if (splitted[0].equals("gmshop"))
            MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        else if (splitted[0].equals("heal"))
            player.setHpMp(30000);
        else if (splitted[0].equals("id"))
            try {
                BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + splitted[1] + "&check=true").openConnection().getInputStream()));
                String s;
                while ((s = dis.readLine()) != null)
                    player.dropMessage(s);
                dis.close();
            } catch (Exception e) {
            }
        else if (splitted[0].equals("item")) {
            int itemId = Integer.parseInt(splitted[1]);
            short quantity = 1;
            try {
                quantity = Short.parseShort(splitted[2]);
            } catch (Exception e) {
            }
                if (itemId >= 5000000 && itemId < 5000065){
                    MaplePet.createPet(itemId);
                }else{
                    MapleInventoryManipulator.addById(c, itemId, quantity, player.getName(), -1);
                IItem item3 = player.getInventory(MapleInventoryType.getByType((byte) (itemId / 1000000))).findById(itemId);
                }

        } else if (splitted[0].equals("drop")) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			int itemId = Integer.parseInt(splitted[1]);
			short quantity = (short) StringUtil.getOptionalIntArg(splitted, 2, 1);
			IItem toDrop;
			if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
				toDrop = ii.getEquipById(itemId);
			} else {
				toDrop = new Item(itemId, (byte) 0, (short) quantity);
			}
			StringBuilder logMsg = new StringBuilder("Created by ");
			logMsg.append(c.getPlayer().getName());
			logMsg.append(" using !drop. Quantity: ");
			logMsg.append(quantity);
			//toDrop.log(logMsg.toString(), false);
			toDrop.setOwner(player.getName());
			c.getPlayer().getMap().spawnItemDrop(c.getPlayer().getObjectId(), c.getPlayer().getPosition(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
        } else if (splitted[0].equals("job"))
            player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
        else if (splitted[0].equals("kill"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setHpMp(0);
        else if (splitted[0].equals("level")) {
            player.setLevel(Integer.parseInt(splitted[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, 0);
        } else if (splitted[0].equals("levelup"))
            player.gainExp(ExpTable.getExpNeededForLevel(player.getLevel()) - player.getExp(), false, false);
        else if (splitted[0].equals("maxstat")) {
            final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
            execute(c, s, heading);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (splitted[0].equals("maxskills"))
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren())
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    if (skill.getId() < 1009 || skill.getId() > 1011)
                        player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
        else if (splitted[0].equals("mesos"))
            player.gainMeso(Integer.parseInt(splitted[1]), true);

       /* else if (splitted[0].equals("notice"))
            try {
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[Notice] " + joinStringFrom(splitted, 1)).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }*/



        else if (splitted[0].equals("onlinechan")) {
            String s = "Characters online (" + cserv.getPlayerStorage().getAllCharacters().size() + ") : ";
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters())
                s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
            player.dropMessage(s.substring(0, s.length() - 2));
        } else if (splitted[0].equals("pap"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
        else if (splitted[0].equals("pianus"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
        else if (splitted[0].equals("servermessage"))
        {
            for (int i = 1; i <= ChannelServer.getAllInstances().size(); i++)
                ChannelServer.getInstance(i).setServerMessage(joinStringFrom(splitted, 1));
        }
        else if (splitted[0].equals("setall")) {
            final int x = Short.parseShort(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (splitted[0].equals("sp")) {
            player.setRemainingSp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
      /*  } else if (splitted[0].equals("ban")) {
            if(splitted[2] != null){

            try {
                PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, banreason = \'" + splitted[2] + "\' WHERE id = " + MapleCharacter.getIdByName(splitted[1]));
                p.executeUpdate();
                p.close();
            } catch (Exception e) {
                player.message("Failed to ban " + splitted[1]);
                return true;
       *             MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.getClient().disconnect();
            player.message("banned " + splitted[1]);
            }else{
                player.message("give a reason");
            }
            }*/

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
                } else if (splitted[0].equals("!reloadops")) {
                    try {
                            ExternalCodeTableGetter.populateValues(SendPacketOpcode.getDefaultProperties(), SendPacketOpcode.values(), true);
                            ExternalCodeTableGetter.populateValues(RecvPacketOpcode.getDefaultProperties(), RecvPacketOpcode.values(), false);
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
                    PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
                    PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
                } else if (splitted[0].equalsIgnoreCase("horntail")) {
            for (int i = 8810002; i < 8810010; i++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(i), player.getPosition());
                }
                else if (splitted[0].equals("say"))
            try {
                   cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, player.getName() + ": " + joinStringFrom(splitted, 1)).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            } else {
            return false;
        }
        return true;
    }

    static String joinStringFrom(String arr[], int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1)
                builder.append(" ");
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
