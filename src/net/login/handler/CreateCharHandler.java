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
package net.login.handler;

import client.Item;
import client.Equip;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleSkinColor;
import client.MapleJob;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class CreateCharHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        if (!MapleCharacter.canCreateChar(name)) {
            return;
        }
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setWorld(c.getWorld());
        int job = slea.readInt();
        slea.readShort();
        slea.skip(3);
        int face = slea.readInt();
        int hair = slea.readInt();
        int top = slea.readInt();
        int bottom = 0;
        if (job != 0)
            bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();
        newchar.setFace(face);
        newchar.setHair(hair);
        newchar.setGender(0);
        newchar.setName(name);
        if (job == 2)
            newchar.setSkinColor(MapleSkinColor.getById(3));
        else if (job == 3)
            newchar.setSkinColor(MapleSkinColor.getById(11));
        else
            newchar.setSkinColor(MapleSkinColor.getById(0));
        if (job == 0) {
            newchar.setJob(MapleJob.CITIZEN);
            newchar.setMap(1000000);
        } else if (job == 1) { // 모험가
            newchar.setMap(10000);
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1));
        } else if (job == 2) { // 시그너스
            newchar.setJob(MapleJob.NOBLESSE);
            newchar.setMap(130000000);
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161047, (byte) 0, (short) 1));
        } else if (job == 3) { // 아란
            newchar.setJob(MapleJob.LEGEND);
            newchar.setMap(140000000);
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161048, (byte) 0, (short) 1));
        } else if (job == 4) { // 에반
            newchar.setJob(MapleJob.EVAN1);
            newchar.setMap(1000000);
        } else {
            System.out.println("[CHAR CREATION] A new job ID has been found: " + job);
        }
        MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        Equip eq_top = new Equip(top, (byte) -5, -1);
        eq_top.setWdef((short) 3);
        equip.addFromDB(eq_top.copy());
        if (job != 0) {
            Equip eq_bottom = new Equip(bottom, (byte) -6, -1);
            eq_bottom.setWdef((short) 2);
            equip.addFromDB(eq_bottom.copy());
        }
        Equip eq_shoes = new Equip(shoes, (byte) -7, -1);
        eq_shoes.setWdef((short) 2); //rite? o_O
        equip.addFromDB(eq_shoes.copy());
        Equip eq_weapon = new Equip(weapon, (byte) -11, -1);
        eq_weapon.setWatk((short) 15);
        equip.addFromDB(eq_weapon.copy());
        newchar.saveToDB(false);
        c.getSession().write(MaplePacketCreator.addNewCharEntry(newchar));
    }
}
