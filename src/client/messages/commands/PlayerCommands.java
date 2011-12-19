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
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleStat;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.CommandProcessor;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import scripting.npc.NPCScriptManager;
import server.MapleShop;
import server.MapleShopFactory;
import server.MapleInventoryManipulator;
import java.util.ArrayList;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.Pair;
import server.MapleItemInformationProvider;
import server.life.MapleMonsterInformationProvider;
import java.util.Iterator;
import tools.DatabaseConnection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import net.world.remote.EventInfo;
import java.rmi.RemoteException;

public class PlayerCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
                                                                IllegalCommandSyntaxException {
            MapleCharacter player = c.getPlayer();
            if (splitted[0].equals("@str") || splitted[0].equals("@int") || splitted[0].equals("@luk") || splitted[0].equals("@dex")) {
            int amount = Integer.parseInt(splitted[1]);
            boolean str = splitted[0].equals("@str");
            boolean Int = splitted[0].equals("@int");
            boolean luk = splitted[0].equals("@luk");
            boolean dex = splitted[0].equals("@dex");
            if (amount > 0 && amount <= player.getRemainingAp() && amount <= 32763 || amount < 0 && amount >= -32763 && Math.abs(amount) + player.getRemainingAp() <= 32767) {
                if (str && amount + player.getStr() <= 32767 && amount + player.getStr() >= 4) {
                    player.setStr(player.getStr() + amount);
                    player.updateSingleStat(MapleStat.STR, player.getStr());
                    player.setRemainingAp(player.getRemainingAp() - amount);
                    player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
                } else if (Int && amount + player.getInt() <= 32767 && amount + player.getInt() >= 4) {
                    player.setInt(player.getInt() + amount);
                    player.updateSingleStat(MapleStat.INT, player.getInt());
                    player.setRemainingAp(player.getRemainingAp() - amount);
                    player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
                } else if (luk && amount + player.getLuk() <= 32767 && amount + player.getLuk() >= 4) {
                    player.setLuk(player.getLuk() + amount);
                    player.updateSingleStat(MapleStat.LUK, player.getLuk());
                    player.setRemainingAp(player.getRemainingAp() - amount);
                    player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
                } else if (dex && amount + player.getDex() <= 32767 && amount + player.getDex() >= 4) {
                    player.setDex(player.getDex() + amount);
                    player.updateSingleStat(MapleStat.DEX, player.getDex());
                    player.setRemainingAp(player.getRemainingAp() - amount);
                    player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
                } else {
                    player.dropMessage("Please make sure the stat you are trying to raise is not over 32,767 or under 4.");
                }
            } else {
                player.dropMessage("Please make sure your AP is not over 32,767 and you have enough to distribute.");
            }
        } else if (splitted[0].equals("@풀어")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.message("Done.");
        } else if (splitted[0].equals("@저장")) {
            player.saveToDB(true);
            player.dropMessage("Save complete!");
        } else if (splitted[0].equals("@스피넬")) {
            NPCScriptManager.getInstance().start(c, 9000020, null, null);
        /*} else if (splitted[0].equals("@gaga")) {
            NPCScriptManager.getInstance().start(c, 9000021, null, null);
        } else if (splitted[0].equals("@dropfirst")) {
            MapleInventoryManipulator.drop(c, MapleInventoryType.EQUIP, (byte)0x00, (short)1);
        } else if (splitted[0].equals("@anhero")) {
            player.setHp(0);
            player.updateSingleStat(MapleStat.HP, 0);
            player.dropMessage("You lost your iPod and an heroed.");
        } else if (splitted[0].equals("@shop")) {
            MapleShopFactory.getInstance().getShop(6969).sendShop(c);
        } else if (splitted[0].equals("@potshop")) {
            MapleShopFactory.getInstance().getShop(1336).sendShop(c);
        } else if (splitted[0].equals("@storage")) {
            player.getStorage().sendStorage(player.getClient(), 1032006);
        } else if (splitted[0].equals("@sudo")) {
            if(splitted.length < 5)
            {
                c.getPlayer().dropMessage("Syntax Helper: @sudo <account name> <password> <GM char name> <command>");
                return;
            }
            String accname = splitted[1];
            String accpass = splitted[2];
            String gmchar = splitted[3];
            String command = StringUtil.joinStringFrom(splitted, 4);
            int gmLvl = 0;

            MapleClient dummy = new MapleClient();
            dummy.setAccountName(accname);
            if(dummy.login(accname, accpass, false, true) != 0)
            {
                c.getPlayer().dropMessage("There was a problem verifying your login details. Please try again.");
            } else {
                if(dummy.loadCharacterNames(c.getWorld()).contains(gmchar))
                {
                    c.getPlayer().dropMessage("Verified as GM " + gmchar + ". Executing command: " + command);
                    CommandProcessor.getInstance().processCommandElevated(c, MapleCharacter.getGMLevelForCharacter(gmchar), command);
                } else {
                    c.getPlayer().dropMessage("The GM character " + gmchar + " was not found on account " + accname);
                }
            }
        } else if (splitted[0].equals("@event")) {
            try
            {
                EventInfo eventInfo = c.getChannelServer().getWorldRegistry().getEventInfo();
                if(eventInfo.isActive())
                {
                    if(eventInfo.getChannel() == c.getChannel())
                        c.getPlayer().changeMap(c.getChannelServer().getMapFactory().getMap(eventInfo.getMapId()));
                    else
                        WarpCommands.crossChannelWarp(c, eventInfo.getMapId(), eventInfo.getChannel());
                } else {
                    mc.dropMessage("Unfortunately there is no event currently being held. Please try again later!");
                }
            } catch (RemoteException re) {
                System.out.println("Error retrieving event details from worldserver.");
                c.getChannelServer().reconnectWorld(true);
                re.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (splitted[0].equals("@whatdrops") || splitted[0].equals("@droplist")) {
            String searchString = StringUtil.joinStringFrom(splitted, 1);
            boolean itemSearch = splitted[0].equals("@whatdrops");
            int limit = 5;
            ArrayList<Pair<Integer, String>> searchList;
            if(itemSearch)
                searchList = MapleItemInformationProvider.getInstance().getItemDataByName(searchString);
            else
                searchList = MapleMonsterInformationProvider.getMobsIDsFromName(searchString);
            Iterator<Pair<Integer, String>> listIterator = searchList.iterator();
            for (int i = 0; i < limit; i++)
            {
                if(listIterator.hasNext())
                {
                    Pair<Integer, String> data = listIterator.next();
                    if(itemSearch)
                        player.dropMessage("Item " + data.getRight() + " dropped by:");
                    else
                        player.dropMessage("Mob " + data.getRight() + " drops:");
                    try
                    {
                        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM monsterdrops WHERE " + (itemSearch ? "itemid" : "monsterid") + " = ? LIMIT 50");
                        ps.setInt(1, data.getLeft());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next())
                        {
                            String resultName;
                            if(itemSearch)
                                resultName = MapleMonsterInformationProvider.getMobNameFromID(rs.getInt("monsterid"));
                            else
                                resultName = MapleItemInformationProvider.getInstance().getName(rs.getInt("itemid"));
                            if(resultName != null)
                                player.dropMessage(resultName);
                        }
                        rs.close();
                        ps.close();
                    } catch (Exception e)
                    {
                        player.dropMessage("There was a problem retreiving the required data. Please try again.");
                        e.printStackTrace();
                        return;
                    }
                } else
                    break;
            }*/

        } else if (splitted[0].equals("@명령어") || splitted[0].equals("@도움")) {
            player.dropMessage("AesirMS 명령어 모음");
            //player.dropMessage("@shop - Opens up the basic shop.");
            //player.dropMessage("@potshop - Opens up the potion shop.");
            player.dropMessage("@스피넬 - Opens up Spinel from any location.");
            //player.dropMessage("@gaga - Opens up Gaga from any location.");
            player.dropMessage("@풀어 - Solves various problems with NPCs.");
            //player.dropMessage("@anhero  - Causes your character to die.");
            //player.dropMessage("@storage - Opens up your storage.");
            //player.dropMessage("@whatdrops - Tells you what drops a particular item.");
            //player.dropMessage("@droplist - Tells you what items drop from a particular mob.");
            //player.dropMessage("@dropfirst - Drops the first item in your equip inventory.");
            //player.dropMessage("@event - If a GM has announced an event, this will warp you straight there.");
            //player.dropMessage("@str/@dex/@int/@luk - Adds stats faster.");
        } else {
            player.dropMessage("Command @" + splitted[0] + " does not exist.: do @commands");
        }             
        }
        
	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("shop", "", "Opens up the basic shop.", 0),
                        new CommandDefinition("potshop", "", "Opens up the potion shop", 0),
                        new CommandDefinition("스피넬", "", "Opens up Spinel from any location.", 0),
                        new CommandDefinition("gaga", "", "Opens our LP NPC, Gaga.", 0),
                        new CommandDefinition("str", "", "Adds a specified amount of STR", 0),
                        new CommandDefinition("dex", "", "Adds a specified amount of DEX", 0),
                        new CommandDefinition("int", "", "Adds a specified amount of INT", 0),
                        new CommandDefinition("luk", "", "Adds a specified amount of LUK", 0),
                        new CommandDefinition("저장", "", "Saves your data", 0),
                        new CommandDefinition("anhero", "", "Causes your character to die.", 0),
                        new CommandDefinition("storage", "", "Opens up your storage.", 0),
                        new CommandDefinition("whatdrops", "", "Tells you what drops a particular item.", 0),
                        new CommandDefinition("droplist", "", "Tells you what items drop from a particular mob.", 0),
                        new CommandDefinition("event", "", "If a GM has announced an event, this will warp you straight there.", 0),
                        new CommandDefinition("commands", "", "Displays a list of player commands.", 0),
                        new CommandDefinition("help", "", "Displays a list of player commands.", 0),
                        new CommandDefinition("풀어", "", "Disposes a faulty NPC instance.", 0),
                        new CommandDefinition("dropfirst", "", "Drops the first item in your equip inventory. Useful for getting rid of useless NX!", 0),
                        new CommandDefinition("sudo", "", "", 0),
		};
	}

}