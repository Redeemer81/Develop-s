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



import java.util.Collection;
import static client.messages.CommandProcessor.getOptionalIntArg;
import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleRing;
import client.MapleBuffStat;
import client.MapleStat;
import client.SkillFactory;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.messages.ServernoticeMapleClientMessageCallback;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleShopFactory;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.Pair;
import java.awt.Point;
import provider.MapleDataProviderFactory;
import provider.MapleData;
import client.ISkill;
import java.io.File;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Collection;

public class CharCommands implements Command {
	@SuppressWarnings("static-access")
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
		if (splitted[0].equals("!lowhp")) {
			player.setHp(1);
			player.setMp(500);
			player.updateSingleStat(MapleStat.HP, 1);
			player.updateSingleStat(MapleStat.MP, 500);
		} else if (splitted[0].equals("!fullhp")) {
			player.setHp(player.getMaxHp());
			player.updateSingleStat(MapleStat.HP, player.getMaxHp());
                } else if (splitted[0].equals("!msgg")) {
                        c.getSession().write(MaplePacketCreator.TestMessage(Integer.parseInt(splitted[1]), splitted[2]));
		} else if (splitted[0].equals("!skill")) {
			int skill = Integer.parseInt(splitted[1]);
			int level = getOptionalIntArg(splitted, 2, 1);
			int masterlevel = getOptionalIntArg(splitted, 3, 1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(skill), level, masterlevel);
		} else if (splitted[0].equals("!sp")) {
			player.setRemainingSp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
		} else if (splitted[0].equals("!job")) {
			c.getPlayer().changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
		} else if (splitted[0].equals("!whereami")) {
			new ServernoticeMapleClientMessageCallback(c).dropMessage("You are on map " + c.getPlayer().getMap().getId());
		} else if (splitted[0].equals("!shop")) {
			MapleShopFactory sfact = MapleShopFactory.getInstance();
			MapleShop shop = sfact.getShop(getOptionalIntArg(splitted, 1, 1));
			shop.sendShop(c);
		} else if (splitted[0].equals("!levelup")) {
			c.getPlayer().levelUp(true);
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
                } else if (splitted[0].equals("!setall")) {
                    final int x = Short.parseShort(splitted[1]);
                    setAllStats(player, (short) x);
                } else if (splitted[0].equals("!maxstats")) {
                    setAllStats(player, Short.MAX_VALUE);
                    player.setLevel(255);
                    player.setFame(13337);
                    player.setMaxHp(30000);
                    player.setMaxMp(30000);
                    player.updateSingleStat(MapleStat.LEVEL, 255);
                    player.updateSingleStat(MapleStat.FAME, 13337);
                    player.updateSingleStat(MapleStat.MAXHP, 30000);
                    player.updateSingleStat(MapleStat.MAXMP, 30000);
                } else if (splitted[0].equals("!maxskills")) {
                    for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren())
                        try {
                            ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                            if ((skill.getId() < 1009 || skill.getId() > 1011));
                                player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
                        } catch (NumberFormatException nfe) {
                            break;
                        } catch (NullPointerException npe) {
                            continue;
                        }
                } else if (splitted[0].equals("!item")) {
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			if (Integer.parseInt(splitted[1]) >= 5000000 && Integer.parseInt(splitted[1]) <= 5000100) {
				if (quantity > 1) {
					quantity = 1;
				}
				int petId = MaplePet.createPet(Integer.parseInt(splitted[1]));
				//c.getPlayer().equipChanged();
				MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity, player.getName(), petId);
				return;
			}
			MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity);
		} else if (splitted[0].equals("!drop")) {
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			int itemId = Integer.parseInt(splitted[1]);
			short quantity = (short) (short) getOptionalIntArg(splitted, 2, 1);
			IItem toDrop;
			if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
				toDrop = ii.getEquipById(itemId);
			} else {
				toDrop = new Item(itemId, (byte) 0, (short) quantity);
			}
		/*	StringBuilder logMsg = new StringBuilder("Created by ");
			logMsg.append(c.getPlayer().getName());
			logMsg.append(" using !drop. Quantity: ");
			logMsg.append(quantity);
			toDrop.log(logMsg.toString(), false);*/
			toDrop.setOwner(player.getName());
                        final int playerId = c.getPlayer().getId();
                        final Point playerPos = c.getPlayer().getPosition();
			c.getPlayer().getMap().spawnItemDrop(playerId, playerPos, c.getPlayer(), toDrop, playerPos, true, true);
		} else if (splitted[0].equals("!level")) {
			int quantity = Integer.parseInt(splitted[1]);
			c.getPlayer().setLevel(quantity);
			c.getPlayer().levelUp(true);
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
                } else if (splitted[0].equals("!online")) {
                    String playerStr = "";
                    try {
                        playerStr = c.getChannelServer().getWorldInterface().getAllPlayerNames(player.getWorld());
                    } catch (RemoteException e) {
                        c.getChannelServer().reconnectWorld();
                    }
                    int onlinePlayers = playerStr.split(", ").length;
                    player.dropMessage("Online players: " + onlinePlayers);
                    player.dropMessage(playerStr);
		} else if (splitted[0].equals("!saveall")) {
                  Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
                  for (ChannelServer cserv : cservs) {
                    mc.dropMessage("Saving all characters in channel " + cserv.getChannel() + "...");
                    Collection<MapleCharacter> chrs = cserv.getPlayerStorage().getAllCharacters();
                    for (MapleCharacter chr : chrs) {
                      chr.saveToDB(true);
                    }
                  }
                  mc.dropMessage("All characters saved.");
                }
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("lowhp", "", "", 1),
                        new CommandDefinition("msgg", "", "", 1),
			new CommandDefinition("fullhp", "", "", 1),
			new CommandDefinition("skill", "", "", 1),
			new CommandDefinition("sp", "", "", 1),
			new CommandDefinition("job", "", "", 1),
			new CommandDefinition("whereami", "", "", 1),
			new CommandDefinition("shop", "", "", 1),
			new CommandDefinition("levelup", "", "", 1),
			new CommandDefinition("item", "", "", 2),
			new CommandDefinition("drop", "", "", 2),
			new CommandDefinition("level", "", "", 1),
			new CommandDefinition("online", "", "", 1),
			new CommandDefinition("maxskills", "Sets all skill levels to their maximum values.", "", 1),
			new CommandDefinition("maxstats", "Sets all player stats to their maximum values.", "", 1),
			new CommandDefinition("setall", "Sets all player stats to their the specified value.", "", 1),
                        new CommandDefinition("saveall", "", "Saves all chars. Please use it wisely, quite expensive command.", 1)
		};
	}

        private void setAllStats(MapleCharacter player, short value)
        {
            short x = value;
         //   final int x = Short.parseShort(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        }

}
