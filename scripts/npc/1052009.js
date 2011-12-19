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
/* Shumi JQ Chest #2
*/

function start() {
    prizes = [4020005,4020006,4020007,4020008,4010000];
	if(cm.getPlayer().getPosition().x > -148 && cm.getPlayer().getPosition().x < 421 && cm.getPlayer().getPosition().y < 554){
        if (cm.isQuestStarted(2056) && cm.canHold(4031040)){
            cm.sendNext("Looking carefully into Treasure Chest, there seems to be a stack of papers in there. I reached out my hand and voila, a huge stack of money.");
			return;
        }else if (!cm.canHold(4031040))
		    cm.sendNext("You don't have enough room in your inventory.");
		else{
            cm.gainItem(prizes[parseInt(Math.random() * prizes.length)],1);
            cm.warp(103000100);
		}
	}else
	    cm.sendNext("You're too far.");
    cm.dispose();
}

function action(mode, type, selection){
    cm.gainItem(4031040,1);
	cm.warp(103000100);
	cm.dispose();
}