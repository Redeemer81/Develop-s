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
/**
 * @author  BubblesDev v75 (Moogra)
 * @NPC     Kerny
 * @purpose Warps you out of airplane
 */
var status = 0;

function start() {
    cm.sendYesNo("The plane will be taking off soon, Will you leave now? You will have buy the plane ticket again to come in here.");
}

function action(mode, type, selection) {
    if (mode != 1) {
        if (mode == 0)
            cm.sendOk("Please hold on for a sec. The plane will be taking off soon. Thanks for your patience.");
        cm.dispose();
        return;
    }
    status++;
    if (status == 1)
        cm.sendYesNo("The plane will be taking off soon. Will you leave now? You will have buy the plane ticket again to come in here.");
    else if (status == 2) {
        cm.sendNext("I have already told you the ticket is not refundable. Hope to see you again.");
    } else if (status == 3) {
        cm.warp(540010000);
        cm.dispose();
    }
}