/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.command_old;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import net.channel.ChannelServer;
import scripting.npc.NPCScriptManager;
import server.MapleShopFactory;
import tools.MaplePacketCreator;
import tools.StringUtil;
import java.util.ArrayList;
import server.MapleItemInformationProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import tools.Pair;
import tools.DatabaseConnection;
import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public class PlayerCommands {

    static void execute(MapleClient c, String[] splitted, char heading) {
        ChannelServer cserv = c.getChannelServer();
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equals("woof")) {
            player.message("meow");
        } else if (splitted[0].equals("str") || splitted[0].equals("int") || splitted[0].equals("luk") || splitted[0].equals("dex")) {
            int amount = Integer.parseInt(splitted[1]);
            boolean str = splitted[0].equals("str");
            boolean Int = splitted[0].equals("int");
            boolean luk = splitted[0].equals("luk");
            boolean dex = splitted[0].equals("dex");
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
        } else if (splitted[0].equals("dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.message("Done.");
        } else if (splitted[0].equals("save")) {
            player.saveToDB(true);
            player.dropMessage("Save complete!");
        } else if (splitted[0].equals("spinel")) {
            NPCScriptManager.getInstance().start(c, 9000020, null, null);
        } else if (splitted[0].equals("anhero")) {
            player.setHp(0);
            player.updateSingleStat(MapleStat.HP, 0);
            player.dropMessage("You lost your iPod and an heroed.");
        } else if (splitted[0].equals("shop")) {
            MapleShopFactory.getInstance().getShop(6969).sendShop(c);
        } else if (splitted[0].equals("potshop")) {
            MapleShopFactory.getInstance().getShop(1336).sendShop(c);
        } else if (splitted[0].equals("storage")) {
            player.getStorage().sendStorage(player.getClient(), 1032006);
        } else if (splitted[0].equals("whatdrops") || splitted[0].equals("droplist")) {
            String searchString = StringUtil.joinStringFrom(splitted, 1);
            boolean itemSearch = splitted[0].equals("whatdrops");
            int limit = 5;
            ArrayList<Pair<Integer, String>> searchList;
            if(itemSearch)
                searchList = MapleItemInformationProvider.getInstance().getItemDataByName(searchString);
            else
                searchList = CommandProcessor.getMobsIDsFromName(searchString);
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
                                resultName = CommandProcessor.getMobNameFromID(rs.getInt("monsterid"));
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
            }

        } else if (splitted[0].equals("commands") || splitted[0].equals("help")) {
            player.dropMessage("LocalMS Player commands");
            player.dropMessage("@shop - Opens up the basic shop.");
            player.dropMessage("@potshop - Opens up the potion shop.");
            player.dropMessage("@spinel - Opens up Spinel from any location.");
            player.dropMessage("@dispose - Solves various problems with NPCs.");
            player.dropMessage("@anhero  - Causes your character to die.");
            player.dropMessage("@storage - Opens up your storage.");
            player.dropMessage("@whatdrops - Tells you what drops a particular item.");
            player.dropMessage("@droplist - Tells you what items drop from a particular mob.");
            player.dropMessage("@str/@dex/@int/@luk - Adds stats faster.");
        } else {
            player.message("Command " + heading + splitted[0] + " does not exist.: do @commands");
        }
    }
}
