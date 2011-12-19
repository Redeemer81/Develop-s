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

/*
 * @author BubblesDev v75
*/

var status = 0;
var maps;
var selectedMap = -1;

function start() {
    var where;
    if (cm.getPlayer().getMap().getId() == 251000000) {
        where = "Hello there. How's the traveling so far? I've been transporting travelers like you to Mu Lung in no time, and... are you interested? It's not as stable as the ship, so you'll have to hold on tight, but I can get there much faster than the ship. I'll take you there as long as you pay 500 mesos.";
    } else {
        maps = [251000000, 200000100];
        var mapNames = ["Herb Town", "Orbis"];
        where = "Hi, where do you want to go?"; // NOT GMS LIKE
        for (var i = 0; i < 2; i++) {
            where += "\r\n#L" + i + "# " + mapNames[i] + "#l";
        }
    }
    cm.sendSimple(where)
}

function action(mode, type, selection) {
    if (cm.getPlayer().getMapId() == 251000000) {
        if (cm.getMeso() > 500) {
            cm.warp(250000100);
            cm.gainMeso(-500);
        } else {
            cm.sendOk("You don't have 500 mesos.");
        }
        cm.dispose();
    } else {
        if (status == 0) {
            cm.sendNext ("Alright, see you next time. Take care.");
            selectedMap = selection;
            status++;
        } else if (status == 1) {
            cm.warp(maps[selectedMap], 0);
            cm.dispose();
        }
    }
}

