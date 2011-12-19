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
package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Generic
 */
public final class AutoAssignHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        slea.skip(8);
        int total = 0;
        if (chr.getRemainingAp() < 1) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            int type = slea.readInt();
            int tempVal = slea.readInt();
            total += tempVal;
            if (tempVal < 0 || tempVal > c.getPlayer().getRemainingAp()) {
                return;
            }
            int newVal = 0;
            if (type == 64) {
                newVal = chr.getStr() + tempVal;
                chr.setStr(newVal);
            } else if (type == 128) {
                newVal = chr.getDex() + tempVal;
                chr.setDex(newVal);
            } else if (type == 256) {
                newVal = chr.getInt() + tempVal;
                chr.setInt(newVal);
            } else if (type == 512) {
                newVal = chr.getLuk() + tempVal;
                chr.setLuk(newVal);
            }
            chr.updateSingleStat(type == 64 ? MapleStat.STR : (type == 128 ? MapleStat.DEX : (type == 256 ? MapleStat.INT : (type == 512 ? MapleStat.LUK : null))), newVal);
        }
        chr.setRemainingAp(chr.getRemainingAp() - total);
        chr.updateSingleStat(MapleStat.AVAILABLEAP, chr.getRemainingAp());
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
