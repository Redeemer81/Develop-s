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
/**
 * @author BubblesDev
 * @purpose Mini-Dungeon - Skelegons
 */
var baseid = 240040511;
var dungeonid = 240040800;

function enter(pi) {
    if (pi.getPlayer().getMapId() == baseid) {
        for (var i = 0; i < 30; i++) {
            if (pi.getClient().getChannelServer().getMapFactory().getMap(dungeonid + i).getAllPlayer().size() == 0) {
                pi.warp(dungeonid + i, 0);
                return true;
            }
        }
        pi.getPlayer().messsage("All of the Mini-Dungeons are in use right now, please try again later.");
    } else {
        pi.warp(baseid, "MD00");
    }
    return true;
}
