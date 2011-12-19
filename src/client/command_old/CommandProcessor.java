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

import client.MapleClient;
import java.util.List;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.File;
import server.life.MapleLifeFactory;

public final class CommandProcessor {
    public static final boolean processCommand(final MapleClient c, final String s) {
        if (s.charAt(0) == '!' && c.getPlayer().isGM()) {
            String[] sp = s.split(" ");
            sp[0] = sp[0].toLowerCase().substring(1);
            c.getPlayer().addCommandToList(s);
            if (JrGM.execute(c, sp, '!'))
                return true;
            if (c.getPlayer().gmLevel() >= 2)
                if(GMCommand.execute(c, sp, '!'))
                    return true;
            if (c.getPlayer().gmLevel() >= 3)
                if(AdminCommand.execute(c, sp, '!'))
                    return true;

            return true;
        }
        if (s.charAt(0) == '@') {
            String[] sp = s.split(" ");
            sp[0] = sp[0].toLowerCase().substring(1);
            PlayerCommands.execute(c, sp, '@');
            return true;
        }
        return false;
    }

    public static ArrayList<Pair<Integer, String>> getMobsIDsFromName(String search)
    {
            MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
            ArrayList<Pair<Integer, String>> retMobs = new ArrayList<Pair<Integer, String>>();
            MapleData data = dataProvider.getData("Mob.img");
            List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
            for (MapleData mobIdData : data.getChildren()) {
                int mobIdFromData = Integer.parseInt(mobIdData.getName());
                String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
                mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
            }
            for (Pair<Integer, String> mobPair : mobPairList) {
                if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                    retMobs.add(mobPair);
                }
            }
            return retMobs;
    }
    public static String getMobNameFromID(int id)
    {
        try
        {
            return MapleLifeFactory.getMonster(id).getName();
        } catch (Exception e)
        {
            return null; //nonexistant mob
        }
    }
}
