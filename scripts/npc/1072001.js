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

/* Magician Job Instructor
	Magician 2nd Job Advancement
	Victoria Road : The Forest North of Ellinia (101020000)
*/

status = -1;

function start() {
    if (cm.haveItem(4031012) || (cm.getJobId() >= 210 && cm.getJobId() <= 232)) {
        cm.sendOk("You're truly a hero!");
        cm.dispose();
    } else if (cm.haveItem(4031009))
        cm.sendNext("Hmmm... it is definitely the letter from #b#p1032001##k, so you came all the way here to take the test and make the 2nd job advancement as the magician? Alright, I'll explain the test to you. Don't sweat it too much, though; it's not that complicated.");
    else {
        cm.sendOk("I can show you the way once your ready for it.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    status++;
    if(mode == 0 && type == 0)
        status -= 2;
    if (status == -1){
        start();
        return;
    } else if (mode != 1){
        if(type == 1 && mode == 0)
            cm.sendNext("Come back later.");
        cm.dispose();
        return;
    }
    if (status == 0)
        cm.sendNextPrev("I'll send you to a hidden map. You'll see monsters not normally seen in normal fields. They look the same like the regular ones, but with a totally different attitude. They neither boost your experience level nor provide you with item.");
    else if (status == 1)
        cm.sendNextPrev("You'll be able to acquire a marble called #bDark Marble#k while knocking down those monsters it is a special marble made out of their sinister, evil minds. Collect 30 of those and then go talk to a colleague of mine in there. That's how you pass the test.");    
    else if (status == 2)
        cm.sendYesNo("Once you go inside, you can't leave until you take care of your mission. If you die, your experience level will decrease so you better really buckle up and get ready... well, do you want to go for it now?");
    else if (status == 3)
        cm.sendNext("Alright I'll let you in! Defeat the monsters, collect 30 Dark Marbles, and then talk to my colleage inside. Then he'll award you the proof of passing the test. #bThe Proff of a Hero#k Good luck.");
    else if (status == 4){
	    cm.dispose();
	    for (var i = 108000200; i < 108000202; i++){
			if(cm.getPlayerCount(i) > 0)
				continue;
			cm.warp(i);
			return;
		}
		cm.sendNext("All the training maps are currently in use. Please try again later.");
    }
}