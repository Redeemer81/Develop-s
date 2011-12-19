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

function start(){
    cm.sendYesNo("You can use the #b#p1061010##k to go back to the real world. Are you sure you want to go back?");
}

function action (mode, type, selection) {
    if (mode == 1)
        cm.warp(cm.getMapId() == 108010301 ? 105070001 : cm.getMapId() == 108010101 ? 105040305 : cm.getMapId() == 108010201 ? 100040106 : cm.getmapId() == 108010401 ? 107000402 : 105070200);
    cm.dispose();
}